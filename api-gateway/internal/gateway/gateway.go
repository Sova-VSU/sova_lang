package gateway

import (
	"fmt"
	"net/http"
	"net/http/httputil"
	"net/url"
	"strings"

	"github.com/golang-jwt/jwt/v5"
)

type Config struct {
	Port                   string
	CoreServiceURL         string
	SubscriptionServiceURL string
	JWTSecret              string
}

type Gateway struct {
	coreProxy         *httputil.ReverseProxy
	subscriptionProxy *httputil.ReverseProxy
	jwtSecret         []byte
}

// publicRoutes contains routes that don't require JWT.
// Format: "METHOD /path"
var publicRoutes = map[string]bool{
	"POST /auth/login":    true,
	"POST /auth/register": true,
	"POST /auth/refresh":  true,
	"GET /scenarios":      true,
}

func New(cfg Config) (*Gateway, error) {
	coreURL, err := url.Parse(cfg.CoreServiceURL)
	if err != nil {
		return nil, fmt.Errorf("invalid core service URL: %w", err)
	}

	subURL, err := url.Parse(cfg.SubscriptionServiceURL)
	if err != nil {
		return nil, fmt.Errorf("invalid subscription service URL: %w", err)
	}

	return &Gateway{
		coreProxy:         httputil.NewSingleHostReverseProxy(coreURL),
		subscriptionProxy: httputil.NewSingleHostReverseProxy(subURL),
		jwtSecret:         []byte(cfg.JWTSecret),
	}, nil
}

func (g *Gateway) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	setCORS(w)

	if r.Method == http.MethodOptions {
		w.WriteHeader(http.StatusNoContent)
		return
	}

	// Strip any client-provided X-User-ID to prevent spoofing.
	r.Header.Del("X-User-ID")

	routeKey := r.Method + " " + r.URL.Path
	if !publicRoutes[routeKey] {
		userID, ok := g.authenticate(w, r)
		if !ok {
			return
		}
		r.Header.Set("X-User-ID", userID)
	}

	backend := g.route(r.URL.Path)
	if backend == nil {
		writeError(w, http.StatusNotFound, "not found")
		return
	}

	backend.ServeHTTP(w, r)
}

func (g *Gateway) authenticate(w http.ResponseWriter, r *http.Request) (string, bool) {
	authHeader := r.Header.Get("Authorization")
	if !strings.HasPrefix(authHeader, "Bearer ") {
		writeError(w, http.StatusUnauthorized, "missing or invalid authorization header")
		return "", false
	}

	tokenStr := strings.TrimPrefix(authHeader, "Bearer ")
	token, err := jwt.Parse(tokenStr, func(t *jwt.Token) (any, error) {
		if _, ok := t.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, jwt.ErrSignatureInvalid
		}
		return g.jwtSecret, nil
	})
	if err != nil || !token.Valid {
		writeError(w, http.StatusUnauthorized, "invalid token")
		return "", false
	}

	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok {
		writeError(w, http.StatusUnauthorized, "invalid token claims")
		return "", false
	}

	// JWT "sub" claim stores user ID (MongoDB ObjectId string).
	sub, ok := claims["sub"].(string)
	if !ok {
		writeError(w, http.StatusUnauthorized, "invalid token claims")
		return "", false
	}

	return sub, true
}

func setCORS(w http.ResponseWriter) {
	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS")
	w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization")
}

// route resolves which backend handles the given path.
func (g *Gateway) route(path string) *httputil.ReverseProxy {
	switch {
	case strings.HasPrefix(path, "/auth/"):
		return g.coreProxy
	case strings.HasPrefix(path, "/scenarios"):
		return g.coreProxy
	case strings.HasPrefix(path, "/users/"):
		return g.coreProxy
	case strings.HasPrefix(path, "/subscriptions"):
		return g.subscriptionProxy
	case strings.HasPrefix(path, "/leaderboard"):
		return g.coreProxy
	default:
		return nil
	}
}

func writeError(w http.ResponseWriter, status int, message string) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	fmt.Fprintf(w, `{"message":%q}`, message)
}

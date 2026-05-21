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
	// Strip any client-provided X-User-ID to prevent spoofing.
	r.Header.Del("X-User-ID")

	routeKey := r.Method + " " + r.URL.Path
	if !publicRoutes[routeKey] {
		userID, ok := g.authenticate(w, r)
		if !ok {
			return
		}
		r.Header.Set("X-User-ID", fmt.Sprintf("%d", userID))
	}

	backend := g.route(r.URL.Path)
	if backend == nil {
		writeError(w, http.StatusNotFound, "not found")
		return
	}

	backend.ServeHTTP(w, r)
}

func (g *Gateway) authenticate(w http.ResponseWriter, r *http.Request) (int64, bool) {
	authHeader := r.Header.Get("Authorization")
	if !strings.HasPrefix(authHeader, "Bearer ") {
		writeError(w, http.StatusUnauthorized, "missing or invalid authorization header")
		return 0, false
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
		return 0, false
	}

	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok {
		writeError(w, http.StatusUnauthorized, "invalid token claims")
		return 0, false
	}

	// JWT "sub" claim stores user ID.
	sub, ok := claims["sub"].(float64)
	if !ok {
		writeError(w, http.StatusUnauthorized, "invalid token claims")
		return 0, false
	}

	return int64(sub), true
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
	default:
		return nil
	}
}

func writeError(w http.ResponseWriter, status int, message string) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	fmt.Fprintf(w, `{"message":%q}`, message)
}

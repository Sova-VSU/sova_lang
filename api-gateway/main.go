package main

import (
	"log"
	"net/http"
	"os"

	"api-gateway/internal/gateway"
)

func main() {
	cfg := gateway.Config{
		Port:                   getenv("PORT", "8080"),
		CoreServiceURL:         getenv("CORE_SERVICE_URL", "http://localhost:8081"),
		SubscriptionServiceURL: getenv("SUBSCRIPTION_SERVICE_URL", "http://localhost:8082"),
		JWTSecret:              getenv("JWT_SECRET", "secret"),
	}

	gw, err := gateway.New(cfg)
	if err != nil {
		log.Fatalf("failed to create gateway: %v", err)
	}

	log.Printf("api-gateway starting on :%s", cfg.Port)
	if err := http.ListenAndServe(":"+cfg.Port, gw); err != nil {
		log.Fatal(err)
	}
}

func getenv(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}

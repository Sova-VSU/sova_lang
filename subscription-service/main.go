package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"subscription-service/internal/handler"
	"subscription-service/internal/middleware"
	"subscription-service/internal/repository"
	"subscription-service/internal/service"
	"time"

	"go.mongodb.org/mongo-driver/v2/mongo"
	"go.mongodb.org/mongo-driver/v2/mongo/options"
)

func main() {
	mongoURI := os.Getenv("MONGO_URI")
	if mongoURI == "" {
		mongoURI = "mongodb://localhost:27017"
	}

	jwtSecret := os.Getenv("JWT_SECRET")
	if jwtSecret == "" {
		jwtSecret = "sova-dev-jwt-secret-key-min-32-chars"
	}

	db, err := connectMongo(mongoURI)
	if err != nil {
		log.Fatalf("failed to connect to mongodb: %v", err)
	}

	repo, err := repository.NewMongoSubscriptionRepository(db)
	if err != nil {
		log.Fatalf("failed to init repository: %v", err)
	}

	svc := service.NewSubscriptionService(repo)
	h := handler.NewSubscriptionHandler(svc)

	mux := http.NewServeMux()
	h.RegisterRoutes(mux, middleware.Auth(jwtSecret))

	log.Println("server starting on :8080")
	if err := http.ListenAndServe(":8082", mux); err != nil {
		log.Fatal(err)
	}
}

func connectMongo(uri string) (*mongo.Database, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	client, err := mongo.Connect(options.Client().ApplyURI(uri))
	if err != nil {
		return nil, err
	}

	if err := client.Ping(ctx, nil); err != nil {
		return nil, err
	}

	log.Println("connected to mongodb")
	return client.Database("subscription_service"), nil
}
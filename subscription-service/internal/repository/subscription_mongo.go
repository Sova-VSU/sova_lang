package repository

import (
	"context"
	"subscription-service/internal/domain"
	"time"

	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
	"go.mongodb.org/mongo-driver/v2/mongo/options"
)

type mongoSubscriptionRepository struct {
	col *mongo.Collection
}

type subscriptionDocument struct {
	ID        bson.ObjectID              `bson:"_id,omitempty"`
	UserID    string                     `bson:"user_id"`
	Status    domain.SubscriptionStatus  `bson:"status"`
	StartedAt time.Time                  `bson:"started_at"`
	EndsAt    time.Time                  `bson:"ends_at"`
}

func NewMongoSubscriptionRepository(db *mongo.Database) (domain.SubscriptionRepository, error) {
	col := db.Collection("subscriptions")

	_, err := col.Indexes().CreateOne(context.Background(), mongo.IndexModel{
		Keys:    bson.D{{Key: "user_id", Value: 1}},
		Options: options.Index().SetUnique(false),
	})
	if err != nil {
		return nil, err
	}

	return &mongoSubscriptionRepository{col: col}, nil
}

func (r *mongoSubscriptionRepository) FindByUserID(userID string) (*domain.Subscription, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	var doc subscriptionDocument
	err := r.col.FindOne(ctx, bson.M{"user_id": userID}).Decode(&doc)
	if err == mongo.ErrNoDocuments {
		return nil, domain.ErrNotFound
	}
	if err != nil {
		return nil, err
	}

	return docToSubscription(&doc), nil
}

func (r *mongoSubscriptionRepository) Save(sub *domain.Subscription) error {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	doc := subscriptionToDoc(sub)
	result, err := r.col.InsertOne(ctx, doc)
	if err != nil {
		return err
	}

	sub.ID = result.InsertedID.(bson.ObjectID).Timestamp().Unix()
	return nil
}

func (r *mongoSubscriptionRepository) Update(sub *domain.Subscription) error {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	filter := bson.M{"user_id": sub.UserID}
	update := bson.M{"$set": bson.M{
		"status":  sub.Status,
		"ends_at": sub.EndsAt,
	}}

	res, err := r.col.UpdateOne(ctx, filter, update)
	if err != nil {
		return err
	}
	if res.MatchedCount == 0 {
		return domain.ErrNotFound
	}
	return nil
}

func subscriptionToDoc(sub *domain.Subscription) *subscriptionDocument {
	return &subscriptionDocument{
		UserID:    sub.UserID,
		Status:    sub.Status,
		StartedAt: sub.StartedAt,
		EndsAt:    sub.EndsAt,
	}
}

func docToSubscription(doc *subscriptionDocument) *domain.Subscription {
	return &domain.Subscription{
		ID:        doc.ID.Timestamp().Unix(),
		UserID:    doc.UserID,
		Status:    doc.Status,
		StartedAt: doc.StartedAt,
		EndsAt:    doc.EndsAt,
	}
}
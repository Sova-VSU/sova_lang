package domain

import (
	"errors"
	"time"
)

var ErrNotFound = errors.New("subscription not found")

type SubscriptionStatus string

const (
	StatusActive    SubscriptionStatus = "ACTIVE"
	StatusEnded     SubscriptionStatus = "ENDED"
	StatusCancelled SubscriptionStatus = "CANCELLED"
)

type Subscription struct {
	ID        int64              `json:"id"`
	UserID    int64              `json:"-"`
	Status    SubscriptionStatus `json:"status"`
	StartedAt time.Time          `json:"startedAt"`
	EndsAt    time.Time          `json:"endsAt"`
}

type SubscriptionRepository interface {
	FindByUserID(userID int64) (*Subscription, error)
	Save(sub *Subscription) error
	Update(sub *Subscription) error
}

type SubscriptionService interface {
	Create(userID int64, durationInDays int) (*Subscription, error)
	GetCurrent(userID int64) (*Subscription, error)
	Cancel(userID int64) (*Subscription, error)
}
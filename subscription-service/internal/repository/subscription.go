package repository

import (
	"subscription-service/internal/domain"
	"sync"
)

type subscriptionRepository struct {
	mu    sync.RWMutex
	store map[int64]*domain.Subscription
	seq   int64
}

func NewSubscriptionRepository() domain.SubscriptionRepository {
	return &subscriptionRepository{
		store: make(map[int64]*domain.Subscription),
	}
}

func (r *subscriptionRepository) FindByUserID(userID int64) (*domain.Subscription, error) {
	r.mu.RLock()
	defer r.mu.RUnlock()

	for _, sub := range r.store {
		if sub.UserID == userID {
			return sub, nil
		}
	}
	return nil, domain.ErrNotFound
}

func (r *subscriptionRepository) Save(sub *domain.Subscription) error {
	r.mu.Lock()
	defer r.mu.Unlock()

	r.seq++
	sub.ID = r.seq
	r.store[sub.ID] = sub
	return nil
}

func (r *subscriptionRepository) Update(sub *domain.Subscription) error {
	r.mu.Lock()
	defer r.mu.Unlock()

	if _, ok := r.store[sub.ID]; !ok {
		return domain.ErrNotFound
	}
	r.store[sub.ID] = sub
	return nil
}

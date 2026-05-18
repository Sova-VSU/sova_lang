package service

import (
	"errors"
	"subscription-service/internal/domain"
	"time"
)

type subscriptionService struct {
	repo domain.SubscriptionRepository
}

func NewSubscriptionService(repo domain.SubscriptionRepository) domain.SubscriptionService {
	return &subscriptionService{repo: repo}
}

func (s *subscriptionService) Create(userID int64, durationInDays int) (*domain.Subscription, error) {
	existing, err := s.repo.FindByUserID(userID)
	if err == nil && existing.Status == domain.StatusActive {
		return nil, errors.New("active subscription already exists")
	}

	now := time.Now().UTC()
	sub := &domain.Subscription{
		UserID:    userID,
		Status:    domain.StatusActive,
		StartedAt: now,
		EndsAt:    now.AddDate(0, 0, durationInDays),
	}

	if err := s.repo.Save(sub); err != nil {
		return nil, err
	}
	return sub, nil
}

func (s *subscriptionService) GetCurrent(userID int64) (*domain.Subscription, error) {
	sub, err := s.repo.FindByUserID(userID)
	if err != nil {
		return nil, domain.ErrNotFound
	}

	if time.Now().UTC().After(sub.EndsAt) && sub.Status == domain.StatusActive {
		sub.Status = domain.StatusEnded
		_ = s.repo.Update(sub)
	}

	return sub, nil
}

func (s *subscriptionService) Cancel(userID int64) (*domain.Subscription, error) {
	sub, err := s.repo.FindByUserID(userID)
	if err != nil {
		return nil, domain.ErrNotFound
	}

	if sub.Status != domain.StatusActive {
		return nil, errors.New("no active subscription to cancel")
	}

	sub.Status = domain.StatusCancelled
	if err := s.repo.Update(sub); err != nil {
		return nil, err
	}
	return sub, nil
}
package handler

import (
	"encoding/json"
	"errors"
	"net/http"
	"strconv"
	"strings"
	"subscription-service/internal/domain"
	"subscription-service/internal/middleware"
)

type SubscriptionHandler struct {
	service domain.SubscriptionService
}

func NewSubscriptionHandler(svc domain.SubscriptionService) *SubscriptionHandler {
	return &SubscriptionHandler{service: svc}
}

func (h *SubscriptionHandler) RegisterRoutes(mux *http.ServeMux, authMiddleware func(http.Handler) http.Handler) {
	mux.Handle("POST /subscriptions", authMiddleware(http.HandlerFunc(h.create)))
	mux.Handle("GET /subscriptions/current", authMiddleware(http.HandlerFunc(h.getCurrent)))
	mux.Handle("DELETE /subscriptions/current", authMiddleware(http.HandlerFunc(h.cancel)))

	// internal endpoint for service-to-service calls
	mux.HandleFunc("GET /internal/subscriptions/user/{userID}", h.getByUserID)
}

func (h *SubscriptionHandler) create(w http.ResponseWriter, r *http.Request) {
	userID, ok := middleware.UserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req struct {
		DurationInDays int `json:"durationInDays"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil || req.DurationInDays < 1 {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	sub, err := h.service.Create(userID, req.DurationInDays)
	if err != nil {
		if strings.Contains(err.Error(), "already exists") {
			writeError(w, http.StatusConflict, err.Error())
			return
		}
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}

	writeJSON(w, http.StatusCreated, sub)
}

func (h *SubscriptionHandler) getCurrent(w http.ResponseWriter, r *http.Request) {
	userID, ok := middleware.UserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	sub, err := h.service.GetCurrent(userID)
	if err != nil {
		writeError(w, http.StatusNotFound, "subscription not found")
		return
	}

	writeJSON(w, http.StatusOK, sub)
}

func (h *SubscriptionHandler) cancel(w http.ResponseWriter, r *http.Request) {
	userID, ok := middleware.UserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	sub, err := h.service.Cancel(userID)
	if err != nil {
		if errors.Is(err, domain.ErrNotFound) {
			writeError(w, http.StatusNotFound, err.Error())
			return
		}
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}

	writeJSON(w, http.StatusOK, sub)
}

func (h *SubscriptionHandler) getByUserID(w http.ResponseWriter, r *http.Request) {
	idStr := r.PathValue("userID")
	userID, err := strconv.ParseInt(idStr, 10, 64)
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid user id")
		return
	}

	sub, err := h.service.GetCurrent(userID)
	if err != nil {
		writeError(w, http.StatusNotFound, "subscription not found")
		return
	}

	writeJSON(w, http.StatusOK, sub)
}

func writeJSON(w http.ResponseWriter, status int, v any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	json.NewEncoder(w).Encode(v)
}

func writeError(w http.ResponseWriter, status int, message string) {
	writeJSON(w, status, map[string]string{"message": message})
}
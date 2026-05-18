package ru.vsu.core.service.client;

import ru.vsu.core.model.dto.SubscriptionDto;

public interface SubscriptionClient {
    SubscriptionDto getSubscription(String userId);
}
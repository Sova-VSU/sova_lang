package ru.vsu.core.service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.vsu.core.model.dto.SubscriptionDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionClientImpl implements SubscriptionClient {

    @Value("${subscription.service.url}")
    private String subscriptionServiceUrl;

    private final RestClient restClient;

    @Override
    public SubscriptionDto getSubscription(String userId) {
        try {
            return restClient.get()
                    .uri(subscriptionServiceUrl + "/internal/subscriptions/user/" + userId)
                    .retrieve()
                    .body(SubscriptionDto.class);
        } catch (Exception e) {
            log.warn("Failed to fetch subscription for user {}: {}", userId, e.getMessage());
            return null;
        }
    }
}

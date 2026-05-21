package ru.vsu.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.vsu.core.exception.NotFoundException;
import ru.vsu.core.exception.UnauthorizedException;
import ru.vsu.core.model.dto.SubscriptionDto;
import ru.vsu.core.model.dto.user.UserDto;
import ru.vsu.core.model.dto.user.UserStatsDto;
import ru.vsu.core.model.entity.User;
import ru.vsu.core.repository.UserRepository;
import ru.vsu.core.service.client.SubscriptionClient;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionClient subscriptionClient;

    public UserDto getById(String userId) {
        User user = findUser(userId);
        SubscriptionDto subscription = subscriptionClient.getSubscription(userId);
        return toDto(user, subscription);
    }

    public void delete(String userId) {
        User user = findUser(userId);
        userRepository.delete(user);
    }

    public UserDto updateName(String userId, String name) {
        User user = findUser(userId);
        user.setName(name);
        userRepository.save(user);
        return toDto(user, null);
    }

    public void updatePassword(String userId, String currentPassword, String newPassword) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserStatsDto getStats(String userId) {
        User user = findUser(userId);
        int completedCount = user.getCompletedScenarios() != null ? user.getCompletedScenarios().size() : 0;
        int level = user.getTotalXp() / 100 + 1;
        return UserStatsDto.builder()
                .totalXp(user.getTotalXp())
                .streak(user.getStreak())
                .completedScenariosCount(completedCount)
                .level(level)
                .build();
    }

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserDto toDto(User user, SubscriptionDto subscription) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .subscription(subscription)
                .build();
    }
}

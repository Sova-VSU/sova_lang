package ru.vsu.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.vsu.core.exception.ForbiddenException;
import ru.vsu.core.exception.NotFoundException;
import ru.vsu.core.model.dto.SubscriptionDto;
import ru.vsu.core.model.dto.scenario.PaginatedScenarioList;
import ru.vsu.core.model.dto.scenario.PaginationMeta;
import ru.vsu.core.model.dto.scenario.ScenarioDto;
import ru.vsu.core.model.dto.scenario.ScenarioListItem;
import ru.vsu.core.model.dto.scenario.ScenarioProgressDto;
import ru.vsu.core.model.entity.Scenario;
import ru.vsu.core.model.entity.User;
import ru.vsu.core.model.entity.UserProgress;
import ru.vsu.core.repository.ScenarioRepository;
import ru.vsu.core.repository.UserProgressRepository;
import ru.vsu.core.repository.UserRepository;
import ru.vsu.core.service.client.SubscriptionClient;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final UserProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final SubscriptionClient subscriptionClient;

    private static final int XP_PER_SCENARIO = 50;

    public PaginatedScenarioList list(int page, int pageSize, Boolean free) {
        PageRequest pageable = PageRequest.of(page - 1, pageSize);
        Page<Scenario> result = (free != null)
                ? scenarioRepository.findByFree(free, pageable)
                : scenarioRepository.findAll(pageable);

        List<ScenarioListItem> items = result.getContent().stream()
                .map(s -> ScenarioListItem.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .description(s.getDescription())
                        .emoji(s.getEmoji())
                        .free(s.isFree())
                        .build())
                .toList();

        return PaginatedScenarioList.builder()
                .items(items)
                .meta(PaginationMeta.builder()
                        .page(page)
                        .pageSize(pageSize)
                        .totalItems(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build();
    }

    public ScenarioDto getById(String scenarioId, String userId) {
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new NotFoundException("Scenario not found"));

        if (!scenario.isFree()) {
            if (userId == null) {
                throw new ForbiddenException("Authentication required");
            }
            SubscriptionDto sub = subscriptionClient.getSubscription(userId);
            if (sub == null || !"ACTIVE".equals(sub.getStatus())) {
                throw new ForbiddenException("Active subscription required");
            }
        }

        return ScenarioDto.builder()
                .id(scenario.getId())
                .title(scenario.getName())
                .description(scenario.getDescription())
                .emoji(scenario.getEmoji())
                .free(scenario.isFree())
                .startStepId(scenario.getStartStepId())
                .completionMessage(scenario.getCompletionMessage())
                .theme(scenario.getTheme())
                .steps(scenario.getSteps())
                .build();
    }

    public ScenarioProgressDto getProgress(String scenarioId, String userId) {
        if (!scenarioRepository.existsById(scenarioId)) {
            throw new NotFoundException("Scenario not found");
        }

        UserProgress progress = progressRepository
                .findByUserIdAndScenarioId(userId, scenarioId)
                .orElse(UserProgress.builder()
                        .userId(userId)
                        .scenarioId(scenarioId)
                        .completedSteps(new ArrayList<>())
                        .completed(false)
                        .xpEarned(0)
                        .startedAt(Instant.now())
                        .lastAccessedAt(Instant.now())
                        .build());

        return ScenarioProgressDto.builder()
                .scenarioId(progress.getScenarioId())
                .currentStepId(progress.getCurrentStepId())
                .completedSteps(progress.getCompletedSteps())
                .completed(progress.isCompleted())
                .xpEarned(progress.getXpEarned())
                .startedAt(progress.getStartedAt())
                .lastAccessedAt(progress.getLastAccessedAt())
                .build();
    }

    public ScenarioProgressDto complete(String scenarioId, String userId) {
        if (!scenarioRepository.existsById(scenarioId)) {
            throw new NotFoundException("Scenario not found");
        }

        UserProgress progress = progressRepository
                .findByUserIdAndScenarioId(userId, scenarioId)
                .orElse(UserProgress.builder()
                        .userId(userId)
                        .scenarioId(scenarioId)
                        .completedSteps(new ArrayList<>())
                        .xpEarned(0)
                        .startedAt(Instant.now())
                        .build());

        progress.setCompleted(true);
        progress.setXpEarned(XP_PER_SCENARIO);
        progress.setLastAccessedAt(Instant.now());
        progressRepository.save(progress);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getCompletedScenarios().contains(scenarioId)) {
            user.getCompletedScenarios().add(scenarioId);
            user.setTotalXp(user.getTotalXp() + XP_PER_SCENARIO);
        }

        LocalDate today = LocalDate.now();
        LocalDate lastActive = user.getLastActiveDate();
        if (lastActive == null || lastActive.isBefore(today.minusDays(1))) {
            user.setStreak(1);
        } else if (lastActive.isBefore(today)) {
            user.setStreak(user.getStreak() + 1);
        }
        user.setLastActiveDate(today);
        userRepository.save(user);

        return ScenarioProgressDto.builder()
                .scenarioId(progress.getScenarioId())
                .currentStepId(progress.getCurrentStepId())
                .completedSteps(progress.getCompletedSteps())
                .completed(progress.isCompleted())
                .xpEarned(progress.getXpEarned())
                .startedAt(progress.getStartedAt())
                .lastAccessedAt(progress.getLastAccessedAt())
                .build();
    }
}

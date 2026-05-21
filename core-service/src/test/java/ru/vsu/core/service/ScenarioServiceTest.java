package ru.vsu.core.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.vsu.core.exception.ForbiddenException;
import ru.vsu.core.exception.NotFoundException;
import ru.vsu.core.model.dto.SubscriptionDto;
import ru.vsu.core.model.dto.scenario.PaginatedScenarioList;
import ru.vsu.core.model.dto.scenario.ScenarioDto;
import ru.vsu.core.model.dto.scenario.ScenarioProgressDto;
import ru.vsu.core.model.entity.Scenario;
import ru.vsu.core.model.entity.UserProgress;
import ru.vsu.core.repository.ScenarioRepository;
import ru.vsu.core.repository.UserProgressRepository;
import ru.vsu.core.service.client.SubscriptionClient;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScenarioServiceTest {

    @Mock
    private ScenarioRepository scenarioRepository;
    @Mock
    private UserProgressRepository progressRepository;
    @Mock
    private SubscriptionClient subscriptionClient;

    @InjectMocks
    private ScenarioService scenarioService;

    @Test
    void list_noFilter_returnsAll() {
        Scenario s = Scenario.builder().id("coffee").name("Кофейня").free(true).build();
        when(scenarioRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(s)));

        PaginatedScenarioList result = scenarioService.list(1, 20, null);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getId()).isEqualTo("coffee");
    }

    @Test
    void list_freeFilter_returnsFreeOnly() {
        Scenario s = Scenario.builder().id("coffee").name("Кофейня").free(true).build();
        when(scenarioRepository.findByFree(eq(true), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(s)));

        PaginatedScenarioList result = scenarioService.list(1, 20, true);

        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    void getById_freeScenario_noAuthRequired() {
        Scenario scenario = Scenario.builder().id("coffee").name("Кофейня").free(true).build();
        when(scenarioRepository.findById("coffee")).thenReturn(Optional.of(scenario));

        ScenarioDto dto = scenarioService.getById("coffee", null);

        assertThat(dto.getId()).isEqualTo("coffee");
    }

    @Test
    void getById_paidScenario_withActiveSubscription_returnsScenario() {
        Scenario scenario = Scenario.builder().id("hotel").name("Отель").free(false).build();
        SubscriptionDto sub = SubscriptionDto.builder().status("ACTIVE").build();

        when(scenarioRepository.findById("hotel")).thenReturn(Optional.of(scenario));
        when(subscriptionClient.getSubscription("user-1")).thenReturn(sub);

        ScenarioDto dto = scenarioService.getById("hotel", "user-1");

        assertThat(dto.getId()).isEqualTo("hotel");
    }

    @Test
    void getById_paidScenario_noUserId_throwsForbidden() {
        Scenario scenario = Scenario.builder().id("hotel").name("Отель").free(false).build();
        when(scenarioRepository.findById("hotel")).thenReturn(Optional.of(scenario));

        assertThatThrownBy(() -> scenarioService.getById("hotel", null))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getById_paidScenario_noActiveSubscription_throwsForbidden() {
        Scenario scenario = Scenario.builder().id("hotel").name("Отель").free(false).build();
        SubscriptionDto sub = SubscriptionDto.builder().status("EXPIRED").build();

        when(scenarioRepository.findById("hotel")).thenReturn(Optional.of(scenario));
        when(subscriptionClient.getSubscription("user-1")).thenReturn(sub);

        assertThatThrownBy(() -> scenarioService.getById("hotel", "user-1"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getById_notFound_throwsNotFoundException() {
        when(scenarioRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scenarioService.getById("unknown", null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getProgress_existingProgress_returnsIt() {
        UserProgress progress = UserProgress.builder()
                .scenarioId("coffee")
                .userId("user-1")
                .completed(true)
                .xpEarned(50)
                .startedAt(Instant.now())
                .lastAccessedAt(Instant.now())
                .build();

        when(scenarioRepository.existsById("coffee")).thenReturn(true);
        when(progressRepository.findByUserIdAndScenarioId("user-1", "coffee"))
                .thenReturn(Optional.of(progress));

        ScenarioProgressDto dto = scenarioService.getProgress("coffee", "user-1");

        assertThat(dto.isCompleted()).isTrue();
        assertThat(dto.getXpEarned()).isEqualTo(50);
    }

    @Test
    void getProgress_noProgress_returnsDefault() {
        when(scenarioRepository.existsById("coffee")).thenReturn(true);
        when(progressRepository.findByUserIdAndScenarioId("user-1", "coffee"))
                .thenReturn(Optional.empty());

        ScenarioProgressDto dto = scenarioService.getProgress("coffee", "user-1");

        assertThat(dto.isCompleted()).isFalse();
        assertThat(dto.getXpEarned()).isEqualTo(0);
    }

    @Test
    void getProgress_scenarioNotFound_throwsNotFoundException() {
        when(scenarioRepository.existsById("unknown")).thenReturn(false);

        assertThatThrownBy(() -> scenarioService.getProgress("unknown", "user-1"))
                .isInstanceOf(NotFoundException.class);
    }
}

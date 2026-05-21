package ru.vsu.core.model.dto.scenario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioProgressDto {
    private String scenarioId;
    private String currentStepId;
    private List<String> completedSteps;
    private boolean completed;
    private int xpEarned;
    private Instant startedAt;
    private Instant lastAccessedAt;
}

package ru.vsu.core.model.dto.scenario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioDto {
    private String id;
    private String title;
    private String description;
    private String emoji;
    private boolean free;
    private String startStepId;
    private String completionMessage;
    private Map<String, Object> theme;
    private Map<String, Object> steps;
}

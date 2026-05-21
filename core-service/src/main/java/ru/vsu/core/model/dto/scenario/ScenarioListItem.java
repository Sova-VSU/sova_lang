package ru.vsu.core.model.dto.scenario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioListItem {
    private String id;
    private String name;
    private String description;
    private String emoji;
    private boolean free;
}

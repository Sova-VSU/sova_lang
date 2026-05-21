package ru.vsu.core.model.dto.scenario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedScenarioList {
    private List<ScenarioListItem> items;
    private PaginationMeta meta;
}

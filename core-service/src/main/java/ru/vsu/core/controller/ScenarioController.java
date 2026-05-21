package ru.vsu.core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.vsu.core.model.dto.scenario.PaginatedScenarioList;
import ru.vsu.core.model.dto.scenario.ScenarioDto;
import ru.vsu.core.model.dto.scenario.ScenarioProgressDto;
import ru.vsu.core.service.ScenarioService;

@RestController
@RequestMapping("/scenarios")
@RequiredArgsConstructor
public class ScenarioController {

    private final ScenarioService scenarioService;

    @GetMapping
    public PaginatedScenarioList list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Boolean free
    ) {
        return scenarioService.list(page, pageSize, free);
    }

    @GetMapping("/{scenarioId}")
    public ScenarioDto getById(
            @PathVariable String scenarioId,
            @RequestHeader(value = "X-User-ID", required = false) String userId
    ) {
        return scenarioService.getById(scenarioId, userId);
    }

    @GetMapping("/{scenarioId}/progress")
    public ScenarioProgressDto getProgress(
            @PathVariable String scenarioId,
            @RequestHeader("X-User-ID") String userId
    ) {
        return scenarioService.getProgress(scenarioId, userId);
    }
}

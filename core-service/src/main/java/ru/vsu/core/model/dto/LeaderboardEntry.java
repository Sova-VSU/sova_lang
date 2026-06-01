package ru.vsu.core.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntry {
    private String id;
    private String name;
    private int completedScenariosCount;
    private int totalXp;
    private int rank;
}

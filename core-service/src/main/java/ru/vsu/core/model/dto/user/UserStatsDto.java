package ru.vsu.core.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    private int totalXp;
    private int streak;
    private int completedScenariosCount;
    private int level;
}

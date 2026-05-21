package ru.vsu.core.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "user_progress")
@CompoundIndex(def = "{'userId': 1, 'scenarioId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgress {
    @Id
    private String id;
    private String userId;
    private String scenarioId;
    private String currentStepId;
    @Builder.Default
    private List<String> completedSteps = new ArrayList<>();
    @Builder.Default
    private boolean completed = false;
    @Builder.Default
    private int xpEarned = 0;
    private Instant startedAt;
    private Instant lastAccessedAt;
}

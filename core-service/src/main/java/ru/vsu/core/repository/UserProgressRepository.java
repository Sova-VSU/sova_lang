package ru.vsu.core.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.vsu.core.model.entity.UserProgress;

import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends MongoRepository<UserProgress, String> {
    Optional<UserProgress> findByUserIdAndScenarioId(String userId, String scenarioId);
    List<UserProgress> findByUserId(String userId);
}

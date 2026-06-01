package ru.vsu.core.repository;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.vsu.core.model.dto.LeaderboardEntry;
import ru.vsu.core.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRefreshToken(String refreshToken);

    @Aggregation(pipeline = {
        "{ '$addFields': { 'completedScenariosCount': { '$size': { '$ifNull': ['$completedScenarios', []] } } } }",
        "{ '$project': { 'name': 1, 'totalXp': 1, 'completedScenariosCount': 1 } }",
        "{ '$sort': { 'completedScenariosCount': -1, 'totalXp': -1 } }",
        "{ '$limit': 50 }"
    })
    List<LeaderboardEntry> findLeaderboard();
}

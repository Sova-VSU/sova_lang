package ru.vsu.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.vsu.core.model.entity.Scenario;

public interface ScenarioRepository extends MongoRepository<Scenario, String> {
    Page<Scenario> findByFree(boolean free, Pageable pageable);
}

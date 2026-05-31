package ru.vsu.core.model.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "scenarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scenario {
    @Id
    private String id;
    @JsonAlias("title")
    private String name;
    private String description;
    private String emoji;
    private boolean free;
    private String startStepId;
    private String completionMessage;
    private Map<String, Object> theme;
    private Map<String, Object> steps;
}

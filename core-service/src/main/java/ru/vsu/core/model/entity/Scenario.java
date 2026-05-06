package ru.vsu.core.model.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
@Builder
public class Scenario {
    private String id;
    private String name;
    private String description;
    private boolean free;
    private List<String> partsId;
}

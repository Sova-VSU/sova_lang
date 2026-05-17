package ru.vsu.core.model.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@Builder
public class User {
    private String id;
    private String name;
    private String password;
    private String subscriptionId;
}
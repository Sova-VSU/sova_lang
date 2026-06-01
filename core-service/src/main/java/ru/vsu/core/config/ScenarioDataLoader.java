package ru.vsu.core.config;

import com.mongodb.client.model.ReplaceOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioDataLoader implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;

    private static final String[] SCENARIOS = {
            "coffee", "restaurant", "airport", "hotel", "pharmacy", "doctor"
    };

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var collection = mongoTemplate.getCollection("scenarios");

        for (String name : SCENARIOS) {
            var resource = new ClassPathResource("scenarios/" + name + ".json");
            String json = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Document doc = Document.parse(json);

            doc.put("_id", doc.getString("id"));
            doc.remove("id");
            if (!doc.containsKey("name") && doc.containsKey("title")) {
                doc.put("name", doc.getString("title"));
                doc.remove("title");
            }

            collection.replaceOne(
                    new Document("_id", doc.get("_id")),
                    doc,
                    new ReplaceOptions().upsert(true)
            );
            log.info("Upserted scenario: {}", name);
        }
    }
}

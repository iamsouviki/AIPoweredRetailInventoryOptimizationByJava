
package com.tcs.AIPoweredRetailInventoryOptimization.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcs.AIPoweredRetailInventoryOptimization.Model.EventData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class EventJsonLoader {

    public List<EventData> loadFromClasspath() {
        List<EventData> out = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            InputStream in = getClass().getResourceAsStream("/data/kolkata_events.events.json");
            if (in == null) {
                throw new IllegalStateException("Cannot find /data/kolkata_events.events.json on classpath.");
            }
            JsonNode root = mapper.readTree(in);
            if (root.isArray()) {
                for (JsonNode node : root) {
                    EventData ed = new EventData();
                    ed.setEventId(node.path("_id").asText(null));
                    ed.setEventName(node.path("eventName").asText(null));
                    ed.setEventType(node.path("eventType").asText(null));
                    ed.setLocation(node.path("location").asText(null));
                    ed.setStoreId(node.path("storeId").asText(null));
                    // timestamp: {"$date": "2024-10-10T00:00:00.000Z"}
                    JsonNode ts = node.path("timestamp").path("$date");
                    if (!ts.isMissingNode()) {
                        ed.setStartTime(Instant.parse(ts.asText()));
                        ed.setEndTime(Instant.parse(ts.asText()));
                    }
                    ed.setName(ed.getEventName());
                    ed.setDescription(ed.getEventType());
                    ed.setSource("Local JSON");
                    out.add(ed);
                }
            }
            log.info("Loaded {} events from JSON", out.size());
            return out;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load events from JSON: " + ex.getMessage(), ex);
        }
    }
}

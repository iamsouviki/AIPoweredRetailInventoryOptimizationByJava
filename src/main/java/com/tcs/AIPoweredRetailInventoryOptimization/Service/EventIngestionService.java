
package com.tcs.AIPoweredRetailInventoryOptimization.Service;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.EventData;
import com.tcs.AIPoweredRetailInventoryOptimization.Repository.EventDataRepository;
import com.tcs.AIPoweredRetailInventoryOptimization.util.EventJsonLoader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventIngestionService {

    private final EventJsonLoader eventJsonLoader;
    private final EventDataRepository eventDataRepository;

    /**
     * Initialize events from local JSON once at startup.
     * You can also call ingestFromJson() manually via controller.
     */
    @PostConstruct
    public void init() {
        try {
            ingestFromJson();
        } catch (Exception ex) {
            log.error("Event init failed: {}", ex.getMessage(), ex);
        }
    }

    public List<EventData> ingestFromJson() {
        List<EventData> events = eventJsonLoader.loadFromClasspath();
        // upsert by eventId
        for (EventData e : events) {
            try {
                eventDataRepository.save(e);
            } catch (Exception ex) {
                log.warn("Skipping event {} due to error: {}", e.getEventId(), ex.getMessage());
            }
        }
        log.info("Ingested {} events from local JSON", events.size());
        return events;
    }

    public List<EventData> listAll() {
        return eventDataRepository.findAll();
    }
}

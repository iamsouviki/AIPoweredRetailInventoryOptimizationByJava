package com.tcs.AIPoweredRetailInventoryOptimization.Service;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.EventData;
import com.tcs.AIPoweredRetailInventoryOptimization.Repository.EventDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventIngestionService {

    private final EventDataRepository eventDataRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.api.key}")
    private String googleApiKey;

    /**
     * Fetch events for the next 30 days from primary calendar.
     * Runs daily at 07:00 AM.
     */
    @Scheduled(cron = "0 0 7 * * ?")
    public void dailyEventFetchAndUpsert() {
        String timeMin = Instant.now().toString();
        String timeMax = Instant.now().plusSeconds(30L * 24 * 3600).toString();

        String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/calendar/v3/calendars/primary/events")
                .queryParam("singleEvents", "true")
                .queryParam("orderBy", "startTime")
                .queryParam("timeMin", timeMin)
                .queryParam("timeMax", timeMax)
                .queryParam("key", googleApiKey)
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null || !response.containsKey("items")) {
            log.warn("No events fetched from Google Calendar.");
            return;
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        log.info("Event ingestion: fetched {} events", items.size());

        for (Map<String, Object> ev : items) {
            try {
                String eventId = ev.get("id").toString();
                Optional<EventData> existing = eventDataRepository.findByEventId(eventId);

                EventData ed = existing.orElseGet(() -> EventData.builder().eventId(eventId).build());

                ed.setName((String) ev.getOrDefault("summary", "No title"));
                ed.setDescription((String) ev.getOrDefault("description", ""));
                Map<String, Object> start = (Map<String, Object>) ev.get("start");
                Map<String, Object> end = (Map<String, Object>) ev.get("end");
                String startStr = start != null ? (String) (start.getOrDefault("dateTime", start.get("date"))) : null;
                String endStr = end != null ? (String) (end.getOrDefault("dateTime", end.get("date"))) : null;
                try {
                    if (startStr != null) ed.setStartTime(Instant.parse(startStr));
                } catch (DateTimeParseException ex) {
                    // ignore parse errors for date-only or other formats
                }
                try {
                    if (endStr != null) ed.setEndTime(Instant.parse(endStr));
                } catch (DateTimeParseException ex) {
                    // ignore parse errors
                }

                ed.setSource("Google Calendar");
                eventDataRepository.save(ed);
            } catch (Exception ex) {
                log.error("Error processing event: {}", ex.getMessage(), ex);
            }
        }
    }
}

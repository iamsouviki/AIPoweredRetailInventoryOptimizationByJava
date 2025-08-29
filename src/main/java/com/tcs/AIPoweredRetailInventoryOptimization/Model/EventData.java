package com.tcs.AIPoweredRetailInventoryOptimization.Model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "event_data")
public class EventData {

    @Id
    private String eventId;

    private String storeRegion;     // region or city near the store
    private String name;
    private String description;

    private Instant startTime;
    private Instant endTime;
    private String source;          // e.g., "Google Calendar"

    private String storeId;
    private String eventType;
    private String eventName;
    private String location;
}


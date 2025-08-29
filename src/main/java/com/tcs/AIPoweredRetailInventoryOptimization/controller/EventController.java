
package com.tcs.AIPoweredRetailInventoryOptimization.controller;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.EventData;
import com.tcs.AIPoweredRetailInventoryOptimization.Service.EventIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventIngestionService eventIngestionService;

    @GetMapping("/events")
    public List<EventData> listEvents() {
        return eventIngestionService.listAll();
    }

    @PostMapping("/events/ingest")
    public List<EventData> ingest() {
        return eventIngestionService.ingestFromJson();
    }
}

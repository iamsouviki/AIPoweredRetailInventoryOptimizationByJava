package com.tcs.AIPoweredRetailInventoryOptimization.Model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "weather_data")
public class WeatherData {

    @Id
    private String id;

    private String storeId;        // location-specific
    private LocalDate timestamp;     // when this weather data applies

    private double temperature;
    private double humidity;
    private double precipitation;  // mm or %
    private String condition;      // e.g., sunny, rainy, cloudy
}


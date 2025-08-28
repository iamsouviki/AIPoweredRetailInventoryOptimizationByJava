package com.tcs.AIPoweredRetailInventoryOptimization.Model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "stores")
public class Stores {

    @Id
    private String id;

    private String storeId;
    private String name;

    private Location location;
    private Demographics demographics;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Location {
        private String city;
        private String state;
        private String country;
        private double lat;
        private double lon;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Demographics {
        private String populationDensity;
        private String incomeLevel;
    }
}


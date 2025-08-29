package com.tcs.AIPoweredRetailInventoryOptimization.Model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "inventory")
public class Inventory {

    @Id
    private String inventoryId;

    private String storeId;
    private String productId;

    private int currentStock;
    private int reorderPoint;   // When to trigger reorder alert
    private int safetyStock;    // Buffer to prevent stockouts

    private Instant lastUpdated;
    private int quantityOnHand;
}


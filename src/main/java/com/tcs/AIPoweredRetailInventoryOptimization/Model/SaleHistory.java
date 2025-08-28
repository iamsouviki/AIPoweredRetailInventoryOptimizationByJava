package com.tcs.AIPoweredRetailInventoryOptimization.Model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sale_history")
public class SaleHistory {

    @Id
    private String saleId;

    private String storeId;
    private String productId;

    private int quantity;
    private double price;

    private Instant saleDate;   // timestamp of the sale
}


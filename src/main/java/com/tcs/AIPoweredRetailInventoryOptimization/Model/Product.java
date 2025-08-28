package com.tcs.AIPoweredRetailInventoryOptimization.Model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {

    @Id
    private String productId;

    private String catalogId;   // Link to ProductCatalog
    private String name;
    private String description;

    private double price;
    private boolean seasonal;
    private String supplierId;
}


package com.tcs.AIPoweredRetailInventoryOptimization.Model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "product_catalog")
public class ProductCatalog {

    @Id
    private String catalogId;

    private String categoryName;
    private String description;
}


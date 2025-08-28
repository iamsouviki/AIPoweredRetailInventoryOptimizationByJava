
package com.tcs.AIPoweredRetailInventoryOptimization.Model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "model_metadata")
public class ModelMetadata {
    @Id
    private String id;
    private String productId;
    private Instant trainedAt;
    private String modelPath;
    private String scalerPath;
    private double mae;
    private double rmse;
}

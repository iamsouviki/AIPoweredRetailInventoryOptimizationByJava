
package com.tcs.AIPoweredRetailInventoryOptimization.Repository;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.ModelMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelMetadataRepository extends MongoRepository<ModelMetadata, String> {
    List<ModelMetadata> findByProductIdOrderByTrainedAtDesc(String productId);
}

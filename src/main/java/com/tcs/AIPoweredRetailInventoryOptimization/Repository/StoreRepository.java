package com.tcs.AIPoweredRetailInventoryOptimization.Repository;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.Stores;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends MongoRepository<Stores, String> {
    Optional<Stores> findByStoreId(String storeId);
}

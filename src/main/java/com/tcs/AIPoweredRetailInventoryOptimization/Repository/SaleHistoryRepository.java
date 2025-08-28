package com.tcs.AIPoweredRetailInventoryOptimization.Repository;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.SaleHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SaleHistoryRepository extends MongoRepository<SaleHistory, String> {
    List<SaleHistory> findByProductId(String productId);
    List<SaleHistory> findByStoreId(String storeId);
    List<SaleHistory> findBySaleDateBetween(Instant start, Instant end);
}

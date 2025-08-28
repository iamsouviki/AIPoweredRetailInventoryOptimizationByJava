package com.tcs.AIPoweredRetailInventoryOptimization.Repository;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByStoreId(String storeId);
    List<Order> findByProductId(String productId);
}


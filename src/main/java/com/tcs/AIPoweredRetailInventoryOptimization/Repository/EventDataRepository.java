package com.tcs.AIPoweredRetailInventoryOptimization.Repository;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.EventData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventDataRepository extends MongoRepository<EventData, String> {
    Optional<EventData> findByEventId(String eventId);
}

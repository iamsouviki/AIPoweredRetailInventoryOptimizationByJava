package com.tcs.AIPoweredRetailInventoryOptimization.Repository;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.WeatherData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherDataRepository extends MongoRepository<WeatherData, String> {
    List<WeatherData> findByStoreId(String storeId);
    List<WeatherData> findByTimestampBetween(LocalDate start, LocalDate end);
    Optional<WeatherData> findByStoreIdAndTimestamp(String storeId, LocalDate timestamp);
}

package com.tcs.AIPoweredRetailInventoryOptimization.Service;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.Stores;
import com.tcs.AIPoweredRetailInventoryOptimization.Model.WeatherData;
import com.tcs.AIPoweredRetailInventoryOptimization.Repository.StoreRepository;
import com.tcs.AIPoweredRetailInventoryOptimization.Repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherIngestionService {

    private final StoreRepository storeRepository;
    private final WeatherDataRepository weatherDataRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${weather.api.key}")
    private String weatherApiKey;

    /**
     * For each store, fetch current weather and upsert by storeId + date.
     * Runs daily at 06:00 AM server time (adjust cron to your TZ).
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void dailyWeatherFetchAndUpsert() {
        List<Stores> stores = storeRepository.findAll();
        log.info("Weather ingestion: fetching weather for {} stores", stores.size());
        for (Stores store : stores) {
            try {
                fetchAndUpsertForStore(store);
            } catch (Exception ex) {
                log.error("Weather fetch failed for store {}: {}", store.getStoreId(), ex.getMessage(), ex);
            }
        }
    }

    public void fetchAndUpsertForStore(Stores store) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl("https://api.openweathermap.org/data/2.5/weather")
                .queryParam("lat", store.getLatitude())
                .queryParam("lon", store.getLongitude())
                .queryParam("appid", weatherApiKey)
                .queryParam("units", "metric");

        Map<String, Object> response = restTemplate.getForObject(uri.toUriString(), Map.class);
        if (response == null) {
            log.warn("No response from weather API for store {}", store.getStoreId());
            return;
        }

        Map<String, Object> main = (Map<String, Object>) response.get("main");
        java.util.List<Map<String, Object>> weatherList = (java.util.List<Map<String, Object>>) response.get("weather");

        double temp = main != null && main.get("temp") != null ? ((Number) main.get("temp")).doubleValue() : 0.0;
        double humidity = main != null && main.get("humidity") != null ? ((Number) main.get("humidity")).doubleValue() : 0.0;
        double precip = 0.0;
        String condition = (weatherList != null && !weatherList.isEmpty()) ? (String) weatherList.get(0).get("main") : "unknown";

        LocalDate today = LocalDate.now();

        Optional<WeatherData> existing = weatherDataRepository.findByStoreIdAndTimestamp(store.getStoreId(), today);

        WeatherData wd = existing.orElseGet(() -> WeatherData.builder()
                .storeId(store.getStoreId())
                .timestamp(today)
                .build());

        wd.setTemperature(temp);
        wd.setHumidity(humidity);
        wd.setPrecipitation(precip);
        wd.setCondition(condition);

        weatherDataRepository.save(wd);
        log.debug("Saved weather for store {} date {} temp {}", store.getStoreId(), today, temp);
    }
}

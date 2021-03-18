package de.dhbw.mosbach.webservices.weather.external;

import de.dhbw.mosbach.webservices.ultimap.graphql.types.CoordinateInput;
import de.dhbw.mosbach.webservices.ultimap.graphql.types.WeatherType;
import de.dhbw.mosbach.webservices.weather.external.response.OpenWeatherMapSimplifiedResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DefaultWeatherProvider implements IWeatherProvider {
    private final RestTemplate restTemplate;

    @Value("${ultimap.weather.token}")
    private String apiToken;

    @Value("${ultimap.weather.cache.validity}")
    private int cacheValidFor;

    List<Long> lastRequestsList;

    private CachedWeatherData cache;

    public DefaultWeatherProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.cache = new CachedWeatherData();
        this.lastRequestsList = new ArrayList<>();
    }

    @Override
    public WeatherType getWeather(CoordinateInput coordinate, int timestamp) {

        CacheData cacheData = cache.getCachedData(coordinate);

        if (!apiToken.equals("DUMMY")) {
            // Refresh cache if necessary
            long currentTimestamp = Instant.now().getEpochSecond();

            if (currentTimestamp > cacheData.getTimestamp() + cacheValidFor) {
                cacheData = refreshData(coordinate, timestamp);
                log.info("Refreshed Weather Cache.");
            } else {
                log.debug("Serving fuel-price from cache.");
            }
        }

        WeatherType weatherType = new WeatherType();
        weatherType.setTemp(cacheData.getTemp());
        weatherType.setRain(cacheData.getRain());
        return weatherType;
    }

    private CacheData refreshData(CoordinateInput coordinate, int timestamp) {

        OpenWeatherMapSimplifiedResponse response = null;
        CacheData cacheData = new CacheData();

        for (Long aLong : lastRequestsList) {
            if (aLong + 60 < Instant.now().getEpochSecond())
                lastRequestsList.remove(aLong);
        }

        if (lastRequestsList.size() < 55) {

            lastRequestsList.add(Instant.now().getEpochSecond());
            String url = String.format("https://api.openweathermap.org/data/2.5/onecall?lat=%s&lon=%s&appid=%s", coordinate.getLat(), coordinate.getLon(), apiToken);
            response = restTemplate.getForObject(url, OpenWeatherMapSimplifiedResponse.class);

        } else {
            log.error("Canceled Request to OpenWeatherMap to prevent of API-Key overuse.");
        }

        if (response != null) {
            cacheData.setTimestamp(Instant.now().getEpochSecond());
            cacheData.setTemp(response.getTemp(timestamp));
            cacheData.setRain(response.getRain(timestamp));
            cache.addCache(coordinate, cacheData);
        } else {
            log.error("No Response from OpenWeatherMap");
        }

        return cacheData;
    }
}

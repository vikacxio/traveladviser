package com.kahanchale.traveladviser.service;

import com.kahanchale.traveladviser.client.WeatherClient;
import com.kahanchale.traveladviser.dto.WeatherDto;
import com.kahanchale.traveladviser.dto.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WeatherService {

    @Autowired
    private WeatherClient weatherClient;

    /**
     * Get current temperature for location from OpenWeatherMap API
     */
    public int getTemperature(WeatherResponse weather,Double latitude, Double longitude) {
        if (weather != null && weather.getMain() != null && weather.getMain().getTemperature() != null) {
            return Math.round(weather.getMain().getTemperature().floatValue());
        }
        log.warn("Temperature not available for location: {}, {}", latitude, longitude);
        return 25; // Default fallback
    }


    public WeatherDto getWeatherDetails(Double latitude, Double longitude) {
        WeatherResponse weather = getCompleteWeatherInfo(latitude,longitude);

        int temperature = getTemperature(weather,latitude, longitude);
        String weatherCondition = getWeatherCondition(weather, latitude, longitude);
        Double humidity = getHumidity(weather, latitude, longitude);
        return new WeatherDto(temperature,weatherCondition,humidity);
    }
    public String getWeatherCondition(WeatherResponse weather, Double latitude, Double longitude) {

        if (weather != null && weather.getWeather() != null && !weather.getWeather().isEmpty()) {
            String condition = weather.getWeather().get(0).getMain();
            log.debug("Weather condition at {},{}: {}", latitude, longitude, condition);
            return condition.toLowerCase();
        }
        log.warn("Weather condition not available for location: {}, {}", latitude, longitude);
        return "cloudy"; // Default fallback
    }

    /**
     * Get complete weather information for location
     * Returns rich weather data including humidity, feels_like, etc.
     */
    public WeatherResponse getCompleteWeatherInfo(Double latitude, Double longitude) {
        return weatherClient.getWeather(latitude, longitude);
    }

    /**
     * Get humidity for location
     */
    public Double getHumidity(WeatherResponse weather, Double latitude, Double longitude) {

        if (weather != null && weather.getMain() != null && weather.getMain().getHumidity() != null) {
            return weather.getMain().getHumidity();
        }
        return 50.0; // Default fallback
    }

    /**
     * Get temperature range for location
     */
    public int[] getTemperatureRange(Double latitude, Double longitude) {
        WeatherResponse weather = weatherClient.getWeather(latitude, longitude);
        if (weather != null && weather.getMain() != null) {
            int minTemp = weather.getMain().getTempMin() != null ? 
                    Math.round(weather.getMain().getTempMin().floatValue()) : 0;
            int maxTemp = weather.getMain().getTempMax() != null ? 
                    Math.round(weather.getMain().getTempMax().floatValue()) : 0;
            return new int[]{minTemp, maxTemp};
        }
        return new int[]{0, 0};
    }
}

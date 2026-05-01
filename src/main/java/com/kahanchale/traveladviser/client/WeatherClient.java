package com.kahanchale.traveladviser.client;

import com.kahanchale.traveladviser.dto.WeatherDescription;
import com.kahanchale.traveladviser.dto.WeatherMain;
import com.kahanchale.traveladviser.dto.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class WeatherClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${openweather.api.key:}")
    private String apiKey;

    @Value("${openweather.api.url:https://api.openweathermap.org/data/2.5/weather}")
    private String apiUrl;

    /**
     * Fetch weather data for given coordinates
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @return WeatherResponse with temperature, weather condition, humidity, etc.
     */
    public WeatherResponse getWeather(Double latitude, Double longitude) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("OpenWeather API key not configured. Using mock data.");
                return getMockWeatherData(latitude, longitude);
            }

            // Build URL with parameters
            String url = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .toUriString();

            log.debug("Calling OpenWeather API: lat={}, lon={}", latitude, longitude);


            ResponseEntity<WeatherResponse> response = restTemplate.getForEntity(url, WeatherResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully fetched weather data for location: {}, {}", latitude, longitude);
                return response.getBody();
            } else {
                log.warn("Unexpected response from OpenWeather API: {}", response.getStatusCode());
                return getMockWeatherData(latitude, longitude);
            }

        } catch (RestClientException e) {
            log.error("Error calling OpenWeather API: {}", e.getMessage());
            return getMockWeatherData(latitude, longitude);
        }
    }

    /**
     * Mock weather data for testing or when API is unavailable
     */
    private WeatherResponse getMockWeatherData(Double latitude, Double longitude) {
        WeatherResponse response = new WeatherResponse();
        response.setName("Unknown City");

        // Mock temperature
        WeatherMain main = new WeatherMain();
        main.setTemperature(25.0);
        main.setFeelsLike(24.0);
        main.setTempMin(22.0);
        main.setTempMax(28.0);
        main.setHumidity(60.0);
        response.setMain(main);

        // Mock weather description
        WeatherDescription weather = new WeatherDescription();
        weather.setId(802);
        weather.setMain("Clouds");
        weather.setDescription("scattered clouds");
        response.setWeather(java.util.List.of(weather));

        log.debug("Using mock weather data");
        return response;
    }
}

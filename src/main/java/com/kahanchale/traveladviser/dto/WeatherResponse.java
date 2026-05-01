package com.kahanchale.traveladviser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResponse {
    
    private String name;                    // City name
    
    @JsonProperty("coord")
    private Coordinates coordinates;
    
    private List<WeatherDescription> weather;
    private WeatherMain main;
    
    private Double visibility;
    
    @JsonProperty("wind")
    private Wind windData;
    
    private Integer timezone;
    private Long dt;                        // Unix timestamp
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Coordinates {
        @JsonProperty("lon")
        private Double longitude;
        
        @JsonProperty("lat")
        private Double latitude;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Wind {
        private Double speed;
        private Integer deg;
        private Double gust;
    }
}

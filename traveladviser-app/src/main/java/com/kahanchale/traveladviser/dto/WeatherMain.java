package com.kahanchale.traveladviser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherMain {
    
    @JsonProperty("temp")
    private Double temperature;
    
    @JsonProperty("feels_like")
    private Double feelsLike;
    
    @JsonProperty("temp_min")
    private Double tempMin;
    
    @JsonProperty("temp_max")
    private Double tempMax;
    
    private Double pressure;
    private Double humidity;
}

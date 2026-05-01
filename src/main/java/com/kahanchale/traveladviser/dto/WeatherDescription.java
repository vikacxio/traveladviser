package com.kahanchale.traveladviser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherDescription {
    
    private Integer id;
    private String main;           // "Rain", "Clouds", "Clear", etc.
    private String description;     // "light rain", "scattered clouds", etc.
    private String icon;
}

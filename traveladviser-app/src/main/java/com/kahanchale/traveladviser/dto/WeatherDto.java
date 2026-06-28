package com.kahanchale.traveladviser.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherDto {
    int temperature;
    String weatherCondition;
    Double humidity;
}

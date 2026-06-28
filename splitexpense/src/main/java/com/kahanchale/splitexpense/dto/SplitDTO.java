package com.kahanchale.splitexpense.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SplitDTO {

    private Long userId;
    private BigDecimal amount;
    private BigDecimal percentage;
}
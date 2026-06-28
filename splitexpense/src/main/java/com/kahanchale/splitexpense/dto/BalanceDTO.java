package com.kahanchale.splitexpense.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceDTO {

    private Long userId;
    private Long owesTo;
    private BigDecimal amount;
}
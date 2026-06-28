package com.kahanchale.splitexpense.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionDTO {
    private Long payerId;
    private Long payeeId;
    private BigDecimal amount;
}

package com.kahanchale.splitexpense.dto;

import lombok.Data;

import java.util.List;

@Data
public class SettlementResponse {
    private List<TransactionDTO> transactions;
    private int minimumCount;
}

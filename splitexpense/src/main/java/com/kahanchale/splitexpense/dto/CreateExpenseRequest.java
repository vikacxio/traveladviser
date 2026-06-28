package com.kahanchale.splitexpense.dto;

import com.kahanchale.splitexpense.entity.SplitExpense;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateExpenseRequest {
    @NotNull
    private Long groupId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private Long paidBy;

    @NotNull
    private SplitExpense.SplitType splitType;

    @NotNull
    private String description;

    private List<SplitRequest> splits;

    @Data
    public static class SplitRequest {
        @NotNull
        private Long userId;
        private BigDecimal amount;
        private BigDecimal percentage;
    }
}
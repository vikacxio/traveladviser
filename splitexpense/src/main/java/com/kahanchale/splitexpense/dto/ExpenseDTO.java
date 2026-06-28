package com.kahanchale.splitexpense.dto;

import com.kahanchale.splitexpense.entity.SplitExpense;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExpenseDTO {

    private Long id;
    private String description;
    private BigDecimal amount;
    private Long paidBy;
    private Long groupId;
    private SplitExpense.SplitType splitType;
    private LocalDateTime createdAt;
    private List<SplitDTO> splits;
}
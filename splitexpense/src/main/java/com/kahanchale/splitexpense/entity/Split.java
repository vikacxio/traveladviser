package com.kahanchale.splitexpense.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "splits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Split {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private SplitExpense expense;

    @Column(nullable = false)
    private Long userId; // User ID

    @Column(precision = 10, scale = 2)
    private BigDecimal amount; // For exact splits

    @Column(precision = 5, scale = 2)
    private BigDecimal percentage; // For percentage splits

    // For equal splits, amount will be calculated
}
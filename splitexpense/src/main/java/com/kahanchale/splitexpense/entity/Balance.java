package com.kahanchale.splitexpense.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "balances", uniqueConstraints = @UniqueConstraint(name = "uk_balances_group_pair", columnNames = {"user_id", "owes_to", "group_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "owes_to", nullable = false)
    private Long owesTo; // User ID who is owed money

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private SplitExpenseGroup group;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Version
    private Long version; // For optimistic locking
}
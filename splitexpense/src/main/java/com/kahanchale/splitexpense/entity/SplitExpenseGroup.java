package com.kahanchale.splitexpense.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "split_expense_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SplitExpenseGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Long createdBy; // User ID from main app

    @ElementCollection
    @CollectionTable(name = "split_expense_group_members", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "member_id", nullable = false)
    private List<Long> memberIds;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SplitExpense> expenses;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
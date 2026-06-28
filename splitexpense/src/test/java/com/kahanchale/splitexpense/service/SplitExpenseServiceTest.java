package com.kahanchale.splitexpense.service;

import com.kahanchale.splitexpense.dto.BalanceDTO;
import com.kahanchale.splitexpense.entity.Split;
import com.kahanchale.splitexpense.entity.SplitExpense;
import com.kahanchale.splitexpense.entity.SplitExpenseGroup;
import com.kahanchale.splitexpense.repository.BalanceRepository;
import com.kahanchale.splitexpense.repository.SettlementRepository;
import com.kahanchale.splitexpense.repository.SplitExpenseGroupRepository;
import com.kahanchale.splitexpense.repository.SplitExpenseRepository;
import com.kahanchale.splitexpense.strategy.EqualSplitStrategy;
import com.kahanchale.splitexpense.strategy.SplitStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SplitExpenseServiceTest {

    @Mock
    private SplitExpenseRepository expenseRepository;

    @Mock
    private SplitExpenseGroupRepository groupRepository;

    @Mock
    private BalanceRepository balanceRepository;

    @Mock
    private SettlementRepository settlementRepository;

    private SplitExpenseService service;

    @BeforeEach
    void setUp() {
        Map<SplitExpense.SplitType, SplitStrategy> strategies = Map.of(
                SplitExpense.SplitType.EQUAL, new EqualSplitStrategy()
        );

        service = new SplitExpenseService(
                expenseRepository,
                groupRepository,
                balanceRepository,
                settlementRepository,
                strategies
        );
    }

    @Test
    void getGroupBalancesShouldRecalculateFromExpensesWhenBalanceRowsAreMissing() {
        SplitExpenseGroup group = new SplitExpenseGroup();
        group.setId(10L);
        group.setMemberIds(List.of(1L, 2L, 3L, 4L));

        SplitExpense expense1 = expense(group, 1L, 500L, List.of(125L, 125L, 125L, 125L));
        SplitExpense expense2 = expense(group, 3L, 1000L, List.of(250L, 250L, 250L, 250L));
        SplitExpense expense3 = expense(group, 4L, 2000L, List.of(500L, 500L, 500L, 500L));
        SplitExpense expense4 = expense(group, 2L, 50000L, List.of(12500L, 12500L, 12500L, 12500L));

        when(expenseRepository.findByGroupId(10L)).thenReturn(List.of(expense1, expense2, expense3, expense4));
        when(balanceRepository.findBalancesByGroupId(10L)).thenReturn(List.of());

        List<BalanceDTO> balances = service.getGroupBalances(10L);

        assertThat(balances)
                .extracting(BalanceDTO::getUserId, BalanceDTO::getOwesTo, BalanceDTO::getAmount)
                .contains(
                        tuple(1L, 2L, new BigDecimal("12375.00")),
                        tuple(1L, 3L, new BigDecimal("125.00")),
                        tuple(1L, 4L, new BigDecimal("375.00")),
                        tuple(3L, 2L, new BigDecimal("12250.00")),
                        tuple(4L, 2L, new BigDecimal("12000.00"))
                );
    }

    private SplitExpense expense(SplitExpenseGroup group, Long paidBy, long amount, List<Long> shares) {
        SplitExpense expense = new SplitExpense();
        expense.setGroup(group);
        expense.setPaidBy(paidBy);
        expense.setAmount(BigDecimal.valueOf(amount));
        expense.setSplitType(SplitExpense.SplitType.EQUAL);

        List<Split> splits = new ArrayList<>();
        for (int i = 0; i < group.getMemberIds().size(); i++) {
            Split split = new Split();
            split.setUserId(group.getMemberIds().get(i));
            split.setAmount(BigDecimal.valueOf(shares.get(i)));
            splits.add(split);
        }

        expense.setSplits(splits);
        return expense;
    }
}

package com.kahanchale.splitexpense.service;

import com.kahanchale.splitexpense.dto.CreateExpenseRequest;
import com.kahanchale.splitexpense.dto.ExpenseDTO;
import com.kahanchale.splitexpense.entity.*;
import com.kahanchale.splitexpense.exception.InvalidSplitException;
import com.kahanchale.splitexpense.repository.BalanceRepository;
import com.kahanchale.splitexpense.repository.SplitExpenseGroupRepository;
import com.kahanchale.splitexpense.repository.SplitExpenseRepository;
import com.kahanchale.splitexpense.strategy.SplitStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final SplitExpenseRepository expenseRepository;
    private final SplitExpenseGroupRepository groupRepository;
    private final BalanceRepository balanceRepository;
    private final Map<SplitExpense.SplitType, SplitStrategy> splitStrategies;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ExpenseDTO createExpense(CreateExpenseRequest request) {
        SplitExpenseGroup group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new InvalidSplitException("Group not found"));

        SplitExpense expense = new SplitExpense();
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setPaidBy(request.getPaidBy());
        expense.setGroup(group);
        expense.setSplitType(request.getSplitType());

        // Get participants from splits or assume all group members
        List<Long> participants = request.getSplits() != null ?
                request.getSplits().stream().map(CreateExpenseRequest.SplitRequest::getUserId).collect(Collectors.toList()) :
                List.of(); // TODO: Get from group members

        SplitStrategy strategy = splitStrategies.get(request.getSplitType());
        if (strategy == null) {
            throw new InvalidSplitException("Unsupported split type");
        }

        strategy.validate(expense, participants);
        Map<Long, BigDecimal> splitAmounts = strategy.calculateSplits(expense, participants);

        // Create splits
        List<Split> splits = splitAmounts.entrySet().stream()
                .map(entry -> {
                    Split split = new Split();
                    split.setExpense(expense);
                    split.setUserId(entry.getKey());
                    split.setAmount(entry.getValue());
                    if (request.getSplitType() == SplitExpense.SplitType.PERCENTAGE) {
                        request.getSplits().stream()
                                .filter(s -> s.getUserId().equals(entry.getKey()))
                                .findFirst()
                                .ifPresent(s -> split.setPercentage(s.getPercentage()));
                    }
                    return split;
                })
                .collect(Collectors.toList());

        expense.setSplits(splits);
        SplitExpense saved = expenseRepository.save(expense);

        // Update balances
        updateBalances(saved);

        log.info("Created expense {} for group {}", saved.getId(), group.getId());
        return convertToDTO(saved);
    }

    private void updateBalances(SplitExpense expense) {
        Long payer = expense.getPaidBy();
        for (Split split : expense.getSplits()) {
            if (!split.getUserId().equals(payer)) {
                updateBalance(split.getUserId(), payer, split.getAmount());
            }
        }
    }

    private void updateBalance(Long debtor, Long creditor, BigDecimal amount) {
        Balance balance = balanceRepository.findByUserIdAndOwesTo(debtor, creditor)
                .orElse(new Balance(null, debtor, creditor, BigDecimal.ZERO, null));

        balance.setAmount(balance.getAmount().add(amount));
        balanceRepository.save(balance);

        // Publish event for decoupling
        eventPublisher.publishEvent(new BalanceUpdatedEvent(debtor, creditor, balance.getAmount()));
    }

    public List<ExpenseDTO> getExpensesByGroup(Long groupId) {
        return expenseRepository.findByGroupId(groupId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ExpenseDTO convertToDTO(SplitExpense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setDescription(expense.getDescription());
        dto.setAmount(expense.getAmount());
        dto.setPaidBy(expense.getPaidBy());
        dto.setGroupId(expense.getGroup().getId());
        dto.setSplitType(expense.getSplitType());
        dto.setCreatedAt(expense.getCreatedAt());
        dto.setSplits(expense.getSplits().stream()
                .map(s -> {
                    SplitDTO splitDTO = new SplitDTO();
                    splitDTO.setUserId(s.getUserId());
                    splitDTO.setAmount(s.getAmount());
                    splitDTO.setPercentage(s.getPercentage());
                    return splitDTO;
                })
                .collect(Collectors.toList()));
        return dto;
    }

    public static class BalanceUpdatedEvent {
        private final Long debtor;
        private final Long creditor;
        private final BigDecimal amount;

        public BalanceUpdatedEvent(Long debtor, Long creditor, BigDecimal amount) {
            this.debtor = debtor;
            this.creditor = creditor;
            this.amount = amount;
        }

        // getters
        public Long getDebtor() { return debtor; }
        public Long getCreditor() { return creditor; }
        public BigDecimal getAmount() { return amount; }
    }
}
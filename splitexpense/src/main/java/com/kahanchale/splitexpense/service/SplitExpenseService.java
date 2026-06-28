package com.kahanchale.splitexpense.service;

import com.kahanchale.splitexpense.dto.BalanceDTO;
import com.kahanchale.splitexpense.dto.CreateExpenseRequest;
import com.kahanchale.splitexpense.dto.CreateGroupRequest;
import com.kahanchale.splitexpense.dto.ExpenseDTO;
import com.kahanchale.splitexpense.dto.GroupDTO;
import com.kahanchale.splitexpense.dto.SettlementRequest;
import com.kahanchale.splitexpense.dto.SettlementResponse;
import com.kahanchale.splitexpense.dto.TransactionDTO;
import com.kahanchale.splitexpense.entity.Balance;
import com.kahanchale.splitexpense.entity.Settlement;
import com.kahanchale.splitexpense.entity.Split;
import com.kahanchale.splitexpense.entity.SplitExpense;
import com.kahanchale.splitexpense.entity.SplitExpenseGroup;
import com.kahanchale.splitexpense.exception.InvalidSplitException;
import com.kahanchale.splitexpense.repository.BalanceRepository;
import com.kahanchale.splitexpense.repository.SettlementRepository;
import com.kahanchale.splitexpense.repository.SplitExpenseGroupRepository;
import com.kahanchale.splitexpense.repository.SplitExpenseRepository;
import com.kahanchale.splitexpense.strategy.SplitStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SplitExpenseService {

    private final SplitExpenseRepository expenseRepository;
    private final SplitExpenseGroupRepository groupRepository;
    private final BalanceRepository balanceRepository;
    private final SettlementRepository settlementRepository;
    private final Map<SplitExpense.SplitType, SplitStrategy> splitStrategies;

    @Transactional
    public GroupDTO createGroup(CreateGroupRequest request) {
        if (request.getMemberIds() == null || request.getMemberIds().isEmpty()) {
            throw new InvalidSplitException("Group must have at least one member");
        }

        SplitExpenseGroup group = new SplitExpenseGroup();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setCreatedBy(request.getCreatedBy());
        group.setMemberIds(request.getMemberIds());

        SplitExpenseGroup saved = groupRepository.save(group);
        return toGroupDTO(saved);
    }

    public List<GroupDTO> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(this::toGroupDTO)
                .collect(Collectors.toList());
    }

    public GroupDTO getGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .map(this::toGroupDTO)
                .orElseThrow(() -> new InvalidSplitException("Split expense group not found"));
    }

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

        List<Long> participants = deriveParticipants(group, request);
        List<Split> candidateSplits = buildCandidateSplits(request);

        expense.setSplits(candidateSplits);

        SplitStrategy strategy = splitStrategies.get(request.getSplitType());
        if (strategy == null) {
            throw new InvalidSplitException("Unsupported split type: " + request.getSplitType());
        }

        strategy.validate(expense, participants);
        Map<Long, BigDecimal> splitAmounts = strategy.calculateSplits(expense, participants);

        List<Split> splits = splitAmounts.entrySet().stream()
                .map(entry -> {
                    Split split = new Split();
                    split.setExpense(expense);
                    split.setUserId(entry.getKey());
                    split.setAmount(entry.getValue());
                    if (request.getSplits() != null) {
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
        recalculateBalancesForGroup(saved.getGroup().getId());
        return toExpenseDTO(saved);
    }

    public List<ExpenseDTO> getExpensesByGroup(Long groupId) {
        return expenseRepository.findByGroupId(groupId).stream()
                .map(this::toExpenseDTO)
                .collect(Collectors.toList());
    }

    public List<BalanceDTO> getBalancesForUser(Long userId) {
        List<BalanceDTO> balances = new ArrayList<>();

        balanceRepository.findByUserId(userId).forEach(balance -> {
            BalanceDTO dto = new BalanceDTO();
            dto.setUserId(balance.getUserId());
            dto.setOwesTo(balance.getOwesTo());
            dto.setAmount(balance.getAmount());
            balances.add(dto);
        });

        balanceRepository.findByOwesTo(userId).forEach(balance -> {
            BalanceDTO dto = new BalanceDTO();
            dto.setUserId(balance.getUserId());
            dto.setOwesTo(balance.getOwesTo());
            dto.setAmount(balance.getAmount());
            balances.add(dto);
        });

        return balances;
    }

    public List<BalanceDTO> getGroupBalances(Long groupId) {
        recalculateBalancesForGroup(groupId);
        return balanceRepository.findBalancesByGroupId(groupId).stream()
                .map(balance -> {
                    BalanceDTO dto = new BalanceDTO();
                    dto.setUserId(balance.getUserId());
                    dto.setOwesTo(balance.getOwesTo());
                    dto.setAmount(balance.getAmount());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public SettlementResponse getMinimumSettlements() {
        List<Balance> balances = balanceRepository.findAll();
        Map<Long, BigDecimal> netBalance = new HashMap<>();

        for (Balance balance : balances) {
            // userId owes owesTo the amount, so userId has negative balance, owesTo has positive
            netBalance.merge(balance.getUserId(), balance.getAmount().negate(), BigDecimal::add);
            netBalance.merge(balance.getOwesTo(), balance.getAmount(), BigDecimal::add);
        }

        List<Map.Entry<Long, BigDecimal>> debtors = netBalance.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) < 0)
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .collect(Collectors.toList());

        List<Map.Entry<Long, BigDecimal>> creditors = netBalance.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());

        List<TransactionDTO> settlements = new ArrayList<>();
        int i = 0, j = 0;

        while (i < debtors.size() && j < creditors.size()) {
            Map.Entry<Long, BigDecimal> debtor = debtors.get(i);
            Map.Entry<Long, BigDecimal> creditor = creditors.get(j);
            BigDecimal amount = debtor.getValue().abs().min(creditor.getValue());

            TransactionDTO transaction = new TransactionDTO();
            transaction.setPayerId(debtor.getKey());
            transaction.setPayeeId(creditor.getKey());
            transaction.setAmount(amount);
            settlements.add(transaction);

            debtor.setValue(debtor.getValue().add(amount));
            creditor.setValue(creditor.getValue().subtract(amount));

            if (debtor.getValue().compareTo(BigDecimal.ZERO) == 0) {
                i++;
            }
            if (creditor.getValue().compareTo(BigDecimal.ZERO) == 0) {
                j++;
            }
        }

        SettlementResponse response = new SettlementResponse();
        response.setTransactions(settlements);
        response.setMinimumCount(settlements.size());
        return response;
    }




    public SettlementResponse getMinimumSettlementsForGroup(Long groupId) {
        recalculateBalancesForGroup(groupId);
        List<Balance> balances = balanceRepository.findBalancesByGroupId(groupId);
        Map<Long, BigDecimal> netBalance = new HashMap<>();

        for (Balance balance : balances) {
            // userId owes owesTo the amount, so userId has negative balance, owesTo has positive
            netBalance.merge(balance.getUserId(), balance.getAmount().negate(), BigDecimal::add);
            netBalance.merge(balance.getOwesTo(), balance.getAmount(), BigDecimal::add);
        }

        List<Map.Entry<Long, BigDecimal>> debtors = netBalance.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) < 0)
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .collect(Collectors.toList());

        List<Map.Entry<Long, BigDecimal>> creditors = netBalance.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());

        List<TransactionDTO> settlements = new ArrayList<>();
        int i = 0, j = 0;

        while (i < debtors.size() && j < creditors.size()) {
            Map.Entry<Long, BigDecimal> debtor = debtors.get(i);
            Map.Entry<Long, BigDecimal> creditor = creditors.get(j);
            BigDecimal amount = debtor.getValue().abs().min(creditor.getValue());

            TransactionDTO transaction = new TransactionDTO();
            transaction.setPayerId(debtor.getKey());
            transaction.setPayeeId(creditor.getKey());
            transaction.setAmount(amount);
            settlements.add(transaction);

            debtor.setValue(debtor.getValue().add(amount));
            creditor.setValue(creditor.getValue().subtract(amount));

            if (debtor.getValue().compareTo(BigDecimal.ZERO) == 0) i++;
            if (creditor.getValue().compareTo(BigDecimal.ZERO) == 0) j++;
        }

        SettlementResponse response = new SettlementResponse();
        response.setTransactions(settlements);
        response.setMinimumCount(settlements.size());
        return response;
    }

    public List<TransactionDTO> getUserSettlements(Long userId) {
        return settlementRepository.findByPayerIdOrPayeeId(userId, userId).stream()
                .map(settlement -> {
                    TransactionDTO dto = new TransactionDTO();
                    dto.setPayerId(settlement.getPayerId());
                    dto.setPayeeId(settlement.getPayeeId());
                    dto.setAmount(settlement.getAmount());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void settleTransaction(SettlementRequest request) {
        Balance balance = balanceRepository.findByUserIdAndOwesTo(request.getPayerId(), request.getPayeeId())
                .orElseThrow(() -> new InvalidSplitException("No outstanding balance found for settlement"));

        SplitExpenseGroup group = balance.getGroup();
        BigDecimal remaining = balance.getAmount().subtract(request.getAmount());
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            balanceRepository.delete(balance);
            BigDecimal reversal = remaining.abs();
            Balance reverseBalance = balanceRepository.findByUserIdAndOwesTo(request.getPayeeId(), request.getPayerId())
                    .orElse(new Balance(null, request.getPayeeId(), request.getPayerId(), group, BigDecimal.ZERO, null));
            reverseBalance.setAmount(reverseBalance.getAmount().add(reversal));
            balanceRepository.save(reverseBalance);
        } else if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            balanceRepository.delete(balance);
        } else {
            balance.setAmount(remaining);
            balanceRepository.save(balance);
        }

        Settlement settlement = new Settlement();
        settlement.setPayerId(request.getPayerId());
        settlement.setPayeeId(request.getPayeeId());
        settlement.setAmount(request.getAmount());
        settlementRepository.save(settlement);
    }

    private void recalculateBalancesForGroup(Long groupId) {
        SplitExpenseGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new InvalidSplitException("Group not found"));

        List<Balance> existingBalances = balanceRepository.findBalancesByGroupId(groupId);
        if (!existingBalances.isEmpty()) {
            balanceRepository.deleteAll(existingBalances);
            balanceRepository.flush();
        }

        Map<String, BigDecimal> netBalances = new HashMap<>();

        for (SplitExpense expense : expenseRepository.findByGroupId(groupId)) {
            Long payer = expense.getPaidBy();
            for (Split split : expense.getSplits()) {
                if (split.getUserId().equals(payer)) {
                    continue;
                }

                Long debtor = split.getUserId();
                String directKey = debtor + ":" + payer;
                String reverseKey = payer + ":" + debtor;

                BigDecimal existingDirect = netBalances.getOrDefault(directKey, BigDecimal.ZERO);
                BigDecimal existingReverse = netBalances.getOrDefault(reverseKey, BigDecimal.ZERO);

                BigDecimal netAmount = existingDirect.add(split.getAmount()).subtract(existingReverse);
                if (netAmount.compareTo(BigDecimal.ZERO) > 0) {
                    netBalances.put(directKey, netAmount);
                    netBalances.remove(reverseKey);
                } else if (netAmount.compareTo(BigDecimal.ZERO) < 0) {
                    netBalances.put(reverseKey, netAmount.abs());
                    netBalances.remove(directKey);
                } else {
                    netBalances.remove(directKey);
                    netBalances.remove(reverseKey);
                }
            }
        }

        for (Map.Entry<String, BigDecimal> entry : netBalances.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            String[] parts = entry.getKey().split(":");
            Balance balance = new Balance();
            balance.setUserId(Long.valueOf(parts[0]));
            balance.setOwesTo(Long.valueOf(parts[1]));
            balance.setGroup(group);
            balance.setAmount(entry.getValue());
            balanceRepository.save(balance);
        }
    }


    private List<Long> deriveParticipants(SplitExpenseGroup group, CreateExpenseRequest request) {
        if (request.getSplitType() == SplitExpense.SplitType.EQUAL) {
            return group.getMemberIds();
        }

        if (request.getSplits() == null || request.getSplits().isEmpty()) {
            throw new InvalidSplitException("Split details must be provided for exact or percentage split");
        }

        return request.getSplits().stream()
                .map(CreateExpenseRequest.SplitRequest::getUserId)
                .collect(Collectors.toList());
    }

    private List<Split> buildCandidateSplits(CreateExpenseRequest request) {
        if (request.getSplits() == null || request.getSplits().isEmpty()) {
            return new ArrayList<>();
        }

        return request.getSplits().stream().map(splitRequest -> {
            Split split = new Split();
            split.setUserId(splitRequest.getUserId());
            split.setAmount(splitRequest.getAmount());
            split.setPercentage(splitRequest.getPercentage());
            return split;
        }).collect(Collectors.toList());
    }

    private ExpenseDTO toExpenseDTO(SplitExpense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setDescription(expense.getDescription());
        dto.setAmount(expense.getAmount());
        dto.setPaidBy(expense.getPaidBy());
        dto.setGroupId(expense.getGroup().getId());
        dto.setSplitType(expense.getSplitType());
        dto.setCreatedAt(expense.getCreatedAt());
        dto.setSplits(expense.getSplits().stream().map(split -> {
            com.kahanchale.splitexpense.dto.SplitDTO splitDTO = new com.kahanchale.splitexpense.dto.SplitDTO();
            splitDTO.setUserId(split.getUserId());
            splitDTO.setAmount(split.getAmount());
            splitDTO.setPercentage(split.getPercentage());
            return splitDTO;
        }).collect(Collectors.toList()));
        return dto;
    }

    private GroupDTO toGroupDTO(SplitExpenseGroup group) {
        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setCreatedBy(group.getCreatedBy());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setMemberIds(group.getMemberIds());
        return dto;
    }
}

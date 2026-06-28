package com.kahanchale.splitexpense.strategy;

import com.kahanchale.splitexpense.entity.Split;
import com.kahanchale.splitexpense.entity.SplitExpense;
import com.kahanchale.splitexpense.exception.InvalidSplitException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExactSplitStrategy implements SplitStrategy {

    @Override
    public void validate(SplitExpense expense, List<Long> participants) {
        if (expense.getSplits() == null || expense.getSplits().isEmpty()) {
            throw new InvalidSplitException("Splits must be provided for exact split");
        }

        BigDecimal totalSplitAmount = expense.getSplits().stream()
                .map(Split::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSplitAmount.compareTo(expense.getAmount()) != 0) {
            throw new InvalidSplitException("Split amounts must sum to the total expense amount");
        }
    }

    @Override
    public Map<Long, BigDecimal> calculateSplits(SplitExpense expense, List<Long> participants) {
        Map<Long, BigDecimal> splits = new HashMap<>();
        for (Split split : expense.getSplits()) {
            splits.put(split.getUserId(), split.getAmount());
        }
        return splits;
    }
}
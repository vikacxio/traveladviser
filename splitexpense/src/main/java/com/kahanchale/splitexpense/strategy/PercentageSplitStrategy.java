package com.kahanchale.splitexpense.strategy;

import com.kahanchale.splitexpense.entity.Split;
import com.kahanchale.splitexpense.entity.SplitExpense;
import com.kahanchale.splitexpense.exception.InvalidSplitException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PercentageSplitStrategy implements SplitStrategy {

    @Override
    public void validate(SplitExpense expense, List<Long> participants) {
        if (expense.getSplits() == null || expense.getSplits().isEmpty()) {
            throw new InvalidSplitException("Splits must be provided for percentage split");
        }

        BigDecimal totalPercentage = expense.getSplits().stream()
                .map(Split::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new InvalidSplitException("Percentages must sum to 100%");
        }
    }

    @Override
    public Map<Long, BigDecimal> calculateSplits(SplitExpense expense, List<Long> participants) {
        Map<Long, BigDecimal> splits = new HashMap<>();
        for (Split split : expense.getSplits()) {
            BigDecimal amount = expense.getAmount().multiply(split.getPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            splits.put(split.getUserId(), amount);
        }
        return splits;
    }
}
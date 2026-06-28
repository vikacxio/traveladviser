package com.kahanchale.splitexpense.strategy;

import com.kahanchale.splitexpense.entity.SplitExpense;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SplitStrategy {

    /**
     * Validates the split request based on the strategy.
     */
    void validate(SplitExpense expense, List<Long> participants);

    /**
     * Calculates the split amounts for each participant.
     * Returns a map of userId to amount.
     */
    Map<Long, BigDecimal> calculateSplits(SplitExpense expense, List<Long> participants);
}
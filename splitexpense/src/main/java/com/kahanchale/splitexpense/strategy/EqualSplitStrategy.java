package com.kahanchale.splitexpense.strategy;

import com.kahanchale.splitexpense.entity.SplitExpense;
import com.kahanchale.splitexpense.exception.InvalidSplitException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EqualSplitStrategy implements SplitStrategy {

    @Override
    public void validate(SplitExpense expense, List<Long> participants) {
        if (participants == null || participants.isEmpty()) {
            throw new InvalidSplitException("Participants cannot be empty for equal split");
        }
    }

    @Override
    public Map<Long, BigDecimal> calculateSplits(SplitExpense expense, List<Long> participants) {
        BigDecimal totalAmount = expense.getAmount();
        int numParticipants = participants.size();
        BigDecimal splitAmount = totalAmount.divide(BigDecimal.valueOf(numParticipants), 2, RoundingMode.HALF_UP);

        Map<Long, BigDecimal> splits = new HashMap<>();
        for (Long userId : participants) {
            splits.put(userId, splitAmount);
        }

        // Adjust the last split to account for rounding
        BigDecimal totalSplit = splitAmount.multiply(BigDecimal.valueOf(numParticipants));
        BigDecimal difference = totalAmount.subtract(totalSplit);
        if (difference.compareTo(BigDecimal.ZERO) != 0) {
            Long lastUser = participants.get(numParticipants - 1);
            splits.put(lastUser, splits.get(lastUser).add(difference));
        }

        return splits;
    }
}
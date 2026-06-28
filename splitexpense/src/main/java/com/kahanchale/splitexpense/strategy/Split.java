package com.kahanchale.splitexpense.strategy;

import com.kahanchale.splitexpense.model.User;

import java.util.List;
import java.util.Map;

// Interface defining the behavior for all types of splits
interface Split {
    /**
     * Calculates the split for the given amount among participants based on specific split details.
     *
     * @param amount        The total amount to split.
     * @param participants  The list of users participating in the split.
     * @param splitDetails  Additional details required for the specific split type.
     * @return A map where the key is the User and the value is the amount they owe.
     */
    Map<User, Double> calculateSplit(double amount, List<User> participants, Map<String, Object> splitDetails);
}
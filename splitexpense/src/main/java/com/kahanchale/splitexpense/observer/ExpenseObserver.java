package com.kahanchale.splitexpense.observer;

import com.kahanchale.splitexpense.model.Expense;

// Interface for observers that need to be notified of expense updates.
interface ExpenseObserver {
    // Called when a new expense is added to the system.
    void onExpenseAdded(Expense expense);

    // Called when an expense is updated in the system.
    void onExpenseUpdated(Expense expense);
}
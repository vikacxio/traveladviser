# Split Expense Module

This module provides expense splitting functionality for the Travel Adviser application, similar to Splitwise.

## Features

- **Scalable**: Modular design with strategy pattern for different split types
- **Durable**: ACID transactions with optimistic locking for balances
- **Extensible**: Easy to add new split strategies (equal, exact, percentage)
- **Fault-tolerant**: Graceful error handling and validation
- **Race condition free**: Optimistic locking on balance updates
- **Maintainable**: Clean architecture with SOLID principles
- **Decoupled**: Event-driven balance updates

## Architecture

### Entities
- `SplitExpenseGroup`: Travel groups for splitting expenses
- `SplitExpense`: Individual expenses with split type
- `Split`: How each user is charged
- `Balance`: Outstanding balances between users
- `Settlement`: Records of payments made

### Strategy Pattern
- `SplitStrategy`: Interface for split calculations
- `EqualSplitStrategy`: Divide equally among participants
- `ExactSplitStrategy`: Exact amounts specified
- `PercentageSplitStrategy`: Percentage-based splits

### Services
- `ExpenseService`: Create and manage expenses
- `BalanceService`: Handle balances and settlements

## API Endpoints

### Create Expense
```
POST /api/split-expense/expenses
{
  "description": "Hotel booking",
  "amount": 300.00,
  "paidBy": 1,
  "groupId": 1,
  "splitType": "EQUAL",
  "splits": [
    {"userId": 2},
    {"userId": 3}
  ]
}
```

### Get Expenses by Group
```
GET /api/split-expense/groups/{groupId}/expenses
```

### Get User Balances
```
GET /api/split-expense/users/{userId}/balances
```

### Settle Balance
```
POST /api/split-expense/settle?payerId=2&payeeId=1&amount=150.00
```

## Database Schema

The module uses the same database as the main application. Tables created:
- `split_expense_groups`
- `split_expenses`
- `splits`
- `balances`
- `settlements`

## Usage

1. Create a travel group
2. Add expenses with appropriate split types
3. View balances
4. Settle payments

## SOLID Principles

- **Single Responsibility**: Each class has one job
- **Open-Closed**: New split types via strategy pattern
- **Liskov Substitution**: Strategies interchangeable
- **Interface Segregation**: Focused interfaces
- **Dependency Inversion**: Depends on abstractions
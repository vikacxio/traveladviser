export type SplitType = 'EQUAL' | 'EXACT' | 'PERCENTAGE';

export interface UserDTO {
  id: number;
  email: string;
  name: string;
}

export interface GroupDTO {
  id: number;
  name: string;
  description?: string;
  createdBy: number;
  createdAt: string;
  memberIds: number[];
}

export interface ExpenseDTO {
  id: number;
  description: string;
  amount: number;
  paidBy: number;
  groupId: number;
  splitType: SplitType;
  createdAt: string;
  splits: SplitDTO[];
}

export interface SplitDTO {
  userId: number;
  amount: number;
  percentage?: number;
}

export interface BalanceDTO {
  userId: number;
  owesTo: number;
  amount: number;
}

export interface TransactionDTO {
  payerId: number;
  payeeId: number;
  amount: number;
}

export interface SplitRequest {
  userId: number;
  amount?: number;
  percentage?: number;
}

export interface CreateExpenseRequest {
  groupId: number;
  amount: number;
  paidBy: number;
  splitType: SplitType;
  description: string;
  splits?: SplitRequest[];
}

export interface CreateGroupRequest {
  name: string;
  description?: string;
  createdBy: number;
  memberIds: number[];
}

export interface SettlementRequest {
  payerId: number;
  payeeId: number;
  amount: number;
}

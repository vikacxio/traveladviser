import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpParams } from '@angular/common/http';
import { ApiService } from '../../core/api.service';
import {
  BalanceDTO,
  CreateExpenseRequest,
  CreateGroupRequest,
  ExpenseDTO,
  GroupDTO,
  SettlementRequest,
  TransactionDTO,
  UserDTO
} from './split-expense.models';

@Injectable({ providedIn: 'root' })
export class SplitExpenseApiService {
  private readonly root = '/api/split-expense';
  private cachedUsers: UserDTO[] | null = null;
  private usersCached = false;

  constructor(private api: ApiService) {}

  /**
   * Get all users (cached after first call)
   */
  getAllUsers(): Observable<UserDTO[]> {
    if (this.usersCached && this.cachedUsers) {
      return new Observable(observer => {
        observer.next(this.cachedUsers!);
        observer.complete();
      });
    }
    return new Observable(observer => {
      this.api.get<UserDTO[]>(`${this.root}/users`).subscribe(
        (users) => {
          this.cachedUsers = users;
          this.usersCached = true;
          observer.next(users);
          observer.complete();
        },
        (error) => observer.error(error)
      );
    });
  }

getUsers(currentUserId?: number): Observable<UserDTO[]> {

  let params = new HttpParams();

  if (currentUserId != null) {
    params = params.set(
      'currentUserId',
      currentUserId.toString()
    );
  }

  return this.api.get<UserDTO[]>(
    `${this.root}/users`,
    params
  );
}
  getGroups(): Observable<GroupDTO[]> {
    return this.api.get<GroupDTO[]>(`${this.root}/groups`);
  }

  createGroup(request: CreateGroupRequest): Observable<GroupDTO> {
    return this.api.post<GroupDTO>(`${this.root}/groups`, request);
  }

  getGroupExpenses(groupId: number): Observable<ExpenseDTO[]> {
    return this.api.get<ExpenseDTO[]>(`${this.root}/groups/${groupId}/expenses`);
  }

  createExpense(request: CreateExpenseRequest): Observable<ExpenseDTO> {
    return this.api.post<ExpenseDTO>(`${this.root}/expenses`, request);
  }

  getBalancesForUser(userId: number): Observable<BalanceDTO[]> {
    return this.api.get<BalanceDTO[]>(`${this.root}/balances/${userId}`);
  }

  getGroupBalances(groupId: number): Observable<BalanceDTO[]> {
    return this.api.get<BalanceDTO[]>(`${this.root}/groups/${groupId}/balances`);
  }

  getMinimumSettlements(): Observable<{ transactions: TransactionDTO[]; minimumCount: number }> {
    return this.api.get<{ transactions: TransactionDTO[]; minimumCount: number }>(`${this.root}/settlements/minimum`);
  }

  getGroupMinimumSettlements(groupId: number): Observable<{ transactions: TransactionDTO[]; minimumCount: number }> {
    return this.api.get<{ transactions: TransactionDTO[]; minimumCount: number }>(`${this.root}/groups/${groupId}/settlements/minimum`);
  }

  settleTransaction(request: SettlementRequest): Observable<void> {
    return this.api.post<void>(`${this.root}/settlements`, request);
  }

  getUserSettlements(userId: number): Observable<TransactionDTO[]> {
    return this.api.get<TransactionDTO[]>(`${this.root}/settlements/user/${userId}`);
  }
}

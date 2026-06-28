import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { SplitExpenseApiService } from './split-expense.service';
import { AuthService } from '../../core/auth.service';

import {
  BalanceDTO,
  CreateExpenseRequest,
  CreateGroupRequest,
  ExpenseDTO,
  GroupDTO,
  TransactionDTO,
  UserDTO
} from './split-expense.models';

@Component({
  standalone: true,
  selector: 'app-split-expenses',
  imports: [CommonModule, FormsModule],
  template: `
    <section class="space-y-6">
      <div class="rounded-3xl bg-white p-6 shadow-sm">
        <h1 class="text-2xl font-semibold text-slate-900">
          SplitExpenses
        </h1>

        <p class="mt-2 text-slate-600">
          Track group expenses, balances, and settlements.
        </p>
      </div>

      <div class="grid gap-6 lg:grid-cols-[1fr_2fr]">
        <!-- LEFT SIDEBAR -->
        <aside class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
          <div class="mb-4 flex items-center justify-between">
            <div>
              <h2 class="text-lg font-semibold text-slate-900">
                Groups
              </h2>

              <p class="text-sm text-slate-500">
                Loaded from backend
              </p>
            </div>

            <button
              type="button"
              (click)="startNewGroup()"
              class="rounded-xl bg-slate-900 px-3 py-2 text-xs font-semibold text-white hover:bg-slate-700"
            >
              New
            </button>
          </div>

          <div
            *ngIf="groups.length === 0"
            class="rounded-2xl border border-slate-200 p-4 text-sm text-slate-600"
          >
            No groups found yet.
          </div>

          <div class="space-y-3">
            <button
              *ngFor="let group of groups"
              type="button"
              (click)="selectGroup(group)"
              class="w-full rounded-2xl border p-3 text-left transition"
              [class.border-slate-900]="activeGroup?.id === group.id"
              [class.bg-slate-50]="activeGroup?.id === group.id"
              [class.border-slate-200]="activeGroup?.id !== group.id"
            >
              <p class="font-semibold text-slate-900">
                {{ group.name }}
              </p>

              <p class="mt-1 text-xs text-slate-500">
                Members: {{ group.memberIds.length }}
              </p>

              <p class="mt-2 text-sm text-slate-500">
                Created by {{ getUserName(group.createdBy) }}
              </p>
            </button>
          </div>
        </aside>

        <!-- RIGHT CONTENT -->
        <div class="space-y-6">

          <!-- GROUP INFO -->
          <div
            *ngIf="activeGroup"
            class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm"
          >
            <div class="flex flex-wrap items-center justify-between gap-3">
              <div>
                <h2 class="text-xl font-semibold text-slate-900">
                  {{ activeGroup.name }}
                </h2>

                <p class="text-sm text-slate-500">
                  Members:
                  {{ getGroupMemberNames(activeGroup).join(', ') }}
                </p>
              </div>

              <button
                type="button"
                (click)="refreshGroup()"
                class="rounded-xl border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:border-slate-500"
              >
                Refresh
              </button>
            </div>

            <p class="mt-3 text-sm text-slate-600">
              {{ activeGroup.description || 'No description available.' }}
            </p>
          </div>

          <!-- ADD EXPENSE -->
          <div
            *ngIf="activeGroup"
            class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm"
          >
            <h3 class="text-lg font-semibold text-slate-900">
              Add expense
            </h3>

            <div class="mt-4 grid gap-3 md:grid-cols-2">
              <input
                [(ngModel)]="newExpenseTitle"
                placeholder="Expense title"
                class="rounded-xl border border-slate-300 px-3 py-2"
              />

              <input
                [(ngModel)]="newExpenseAmount"
                type="number"
                placeholder="Amount"
                class="rounded-xl border border-slate-300 px-3 py-2"
              />

              <select
                [(ngModel)]="newExpensePaidBy"
                class="rounded-xl border border-slate-300 px-3 py-2 md:col-span-2"
              >
                <option [ngValue]="null" disabled>
                  Paid by
                </option>

                <option
                  *ngFor="let memberId of activeGroup.memberIds"
                  [ngValue]="memberId"
                >
                  {{ getUserName(memberId) }}
                </option>
              </select>
            </div>

            <button
              type="button"
              (click)="addExpense()"
              class="mt-4 rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-700"
            >
              Add Expense
            </button>
          </div>

          <!-- EXPENSES -->
          <div
            *ngIf="activeGroup"
            class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm"
          >
            <div class="flex items-center justify-between gap-3">
              <h3 class="text-lg font-semibold text-slate-900">
                Recent expenses
              </h3>

              <button
                type="button"
                (click)="showSettlements()"
                class="rounded-xl border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:border-slate-500"
              >
                Show Settlements
              </button>
            </div>

            <div class="mt-4 space-y-3">
              <div
                *ngFor="let expense of expenses"
                class="rounded-2xl border border-slate-100 p-3"
              >
                <div class="flex items-center justify-between gap-3">
                  <p class="font-medium text-slate-900">
                    {{ expense.description }}
                  </p>

                  <p class="text-sm font-semibold text-slate-900">
                    {{ expense.amount | currency:'INR':'symbol':'1.0-0' }}
                  </p>
                </div>

                <p class="mt-1 text-sm text-slate-500">
                  Paid by {{ getUserName(expense.paidBy) }}
                </p>
              </div>
            </div>
          </div>

          <!-- BALANCES -->
          <div
            *ngIf="balances.length > 0"
            class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm"
          >
            <h3 class="text-lg font-semibold text-slate-900">
              Balances
            </h3>

            <div class="mt-4 space-y-3">
             <ng-container *ngFor="let balance of balances">
              <div
                *ngIf="balance.amount > 0.1"
                class="rounded-2xl border border-slate-100 p-3"
              >
                <p class="font-medium text-slate-900">
                  {{ getUserName(balance.userId) }}
                  owes
                  {{ getUserName(balance.owesTo) }}
                </p>

                <p class="mt-1 text-sm text-slate-500">
                  Amount:
                  {{ balance.amount | currency:'INR':'symbol':'1.0-0' }}
                </p>
              </div>
               </ng-container>
            </div>
          </div>

          <!-- SETTLEMENTS -->
          <div
            *ngIf="settlements.length > 0"
            class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm"
          >
            <h3 class="text-lg font-semibold text-slate-900">
              Suggested settlements
            </h3>

            <p class="text-sm text-slate-500">
              Minimum payments needed:
              {{ minimumCount }}
            </p>

            <div class="mt-4 space-y-3">
              <div
                *ngFor="let settlement of settlements"
                class="rounded-2xl border border-slate-100 p-3"
              >
                <p class="font-medium text-slate-900">
                  {{ getUserName(settlement.payerId) }}
                  pays
                  {{ getUserName(settlement.payeeId) }}
                </p>

                <p class="mt-1 text-sm text-slate-500">
                  Amount:
                  {{ settlement.amount | currency:'INR':'symbol':'1.0-0' }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- CREATE GROUP -->
      <div
        *ngIf="creatingGroup"
        class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm"
      >
        <h3 class="text-lg font-semibold text-slate-900">
          Create a new split group
        </h3>

        <div class="mt-4">
          <input
            [(ngModel)]="newGroupName"
            placeholder="Group name"
            class="w-full rounded-xl border border-slate-300 px-3 py-2"
          />
        </div>

        <div class="mt-4">
          <label class="text-sm font-medium text-slate-700">
            Select members:
          </label>

          <div class="mt-3 grid gap-2 md:grid-cols-2">
            <div
              *ngFor="let user of availableUsers"
              class="flex items-center"
            >
              <input
                type="checkbox"
                [id]="'user-' + user.id"
                [checked]="selectedMemberIds.includes(user.id)"
                (change)="toggleUserSelection(user.id)"
                class="h-4 w-4 rounded border-slate-300"
              />

              <label
                [for]="'user-' + user.id"
                class="ml-2 text-sm text-slate-700"
              >
                {{ user.name }} ({{ user.email }})
              </label>
            </div>
          </div>
        </div>

        <div class="mt-4 flex gap-3">
          <button
            type="button"
            (click)="createGroup()"
            class="rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-700"
          >
            Save Group
          </button>

          <button
            type="button"
            (click)="cancelNewGroup()"
            class="rounded-xl border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
          >
            Cancel
          </button>
        </div>
      </div>
    </section>
  `
})
export class SplitExpensesComponent implements OnInit {

  groups: GroupDTO[] = [];
  activeGroup: GroupDTO | null = null;

  expenses: ExpenseDTO[] = [];
  balances: BalanceDTO[] = [];
  settlements: TransactionDTO[] = [];

  minimumCount = 0;

  users: UserDTO[] = [];
  availableUsers: UserDTO[] = [];

  newExpenseTitle = '';
  newExpenseAmount: number | null = null;
  newExpensePaidBy: number | null = null;

  creatingGroup = false;

  newGroupName = '';

  selectedMemberIds: number[] = [];

  currentUserId: number = 0;

  constructor(
    private splitExpenseApi: SplitExpenseApiService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const userId = this.authService.getUserId();
    if (userId) {
      this.currentUserId = userId;
    }
    this.loadUsers();
    this.loadGroups();
  }

  loadUsers(): void {
    // Load all users for name lookups
    this.splitExpenseApi
      .getAllUsers()
      .subscribe((users) => {
        this.users = users;
      });

    // Load available users (excluding current user) for group creation
    this.splitExpenseApi
      .getUsers(this.currentUserId)
      .subscribe((users) => {
        this.availableUsers = users;
      });
  }

  loadGroups(): void {
    this.splitExpenseApi
      .getGroups()
      .subscribe((groups) => {
        this.groups = groups;

        if (!this.activeGroup && groups.length) {
          this.selectGroup(groups[0]);
        }
      });
  }

  refreshGroup(): void {
    if (this.activeGroup) {
      this.selectGroup(this.activeGroup);
    }
  }

  selectGroup(group: GroupDTO): void {
    this.activeGroup = group;

    this.newExpensePaidBy =
      group.memberIds.length
        ? group.memberIds[0]
        : null;

    // Clear previous data - ensures only selected group's data is shown
    this.expenses = [];
    this.balances = [];
    this.settlements = [];
    this.minimumCount = 0;

    this.splitExpenseApi
      .getGroupExpenses(group.id)
      .subscribe((expenses) => {
        this.expenses = expenses;
      });

    this.splitExpenseApi
      .getGroupBalances(group.id)
      .subscribe((balances) => {
        this.balances = balances;
      });
  }

  addExpense(): void {

    if (
      !this.activeGroup ||
      !this.newExpenseTitle.trim() ||
      !this.newExpenseAmount ||
      this.newExpenseAmount <= 0 ||
      !this.newExpensePaidBy
    ) {
      return;
    }

    const request: CreateExpenseRequest = {
      groupId: this.activeGroup.id,
      amount: this.newExpenseAmount,
      paidBy: this.newExpensePaidBy,
      splitType: 'EQUAL',
      description: this.newExpenseTitle.trim(),
      splits: []
    };

    this.splitExpenseApi
      .createExpense(request)
      .subscribe(() => {

        this.newExpenseTitle = '';
        this.newExpenseAmount = null;

        this.newExpensePaidBy =
          this.activeGroup?.memberIds[0] ?? null;

        if (this.activeGroup) {
          this.selectGroup(this.activeGroup);
        }
      });
  }

  showSettlements(): void {
    if (this.activeGroup) {
      this.splitExpenseApi
        .getGroupMinimumSettlements(this.activeGroup.id)
        .subscribe((result) => {
          this.settlements = result.transactions;
          this.minimumCount = result.minimumCount;
        });
    }
  }

  startNewGroup(): void {
    this.creatingGroup = true;
    this.newGroupName = '';
    this.selectedMemberIds = [];
  }

  cancelNewGroup(): void {
    this.creatingGroup = false;
    this.newGroupName = '';
    this.selectedMemberIds = [];
  }

  toggleUserSelection(userId: number): void {

    const index =
      this.selectedMemberIds.indexOf(userId);

    if (index > -1) {
      this.selectedMemberIds.splice(index, 1);
    } else {
      this.selectedMemberIds.push(userId);
    }
  }

  createGroup(): void {

    if (
      !this.newGroupName.trim() ||
      this.selectedMemberIds.length === 0
    ) {
      alert(
        'Please enter a group name and select at least one member'
      );

      return;
    }

    const request: CreateGroupRequest = {
      name: this.newGroupName.trim(),
      description: '',
      createdBy: this.currentUserId,
      memberIds: [
        this.currentUserId,
        ...this.selectedMemberIds
      ]
    };

    this.splitExpenseApi
      .createGroup(request)
      .subscribe(() => {

        this.creatingGroup = false;

        this.newGroupName = '';

        this.selectedMemberIds = [];

        this.loadGroups();
      });
  }

  getUserName(userId: number): string {

    if (userId === this.currentUserId) {
      return 'You';
    }

    const user =
      this.users.find(u => u.id === userId);

    return user
      ? user.name
      : `User ${userId}`;
  }

  getGroupMemberNames(
    group: GroupDTO
  ): string[] {

    return group.memberIds.map(
      id => this.getUserName(id)
    );
  }
}
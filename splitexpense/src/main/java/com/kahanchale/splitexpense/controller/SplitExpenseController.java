package com.kahanchale.splitexpense.controller;

import com.kahanchale.splitexpense.dto.BalanceDTO;
import com.kahanchale.splitexpense.dto.CreateExpenseRequest;
import com.kahanchale.splitexpense.dto.CreateGroupRequest;
import com.kahanchale.splitexpense.dto.ExpenseDTO;
import com.kahanchale.splitexpense.dto.GroupDTO;
import com.kahanchale.splitexpense.dto.SettlementRequest;
import com.kahanchale.splitexpense.dto.SettlementResponse;
import com.kahanchale.splitexpense.dto.TransactionDTO;
import com.kahanchale.splitexpense.service.SplitExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/split-expense")
@RequiredArgsConstructor
public class SplitExpenseController {

    private final SplitExpenseService splitExpenseService;

    @PostMapping("/groups")
    public ResponseEntity<GroupDTO> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.ok(splitExpenseService.createGroup(request));
    }

    @GetMapping("/groups")
    public ResponseEntity<List<GroupDTO>> getGroups() {
        return ResponseEntity.ok(splitExpenseService.getAllGroups());
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<GroupDTO> getGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(splitExpenseService.getGroup(groupId));
    }

    @GetMapping("/groups/{groupId}/expenses")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(splitExpenseService.getExpensesByGroup(groupId));
    }

    @PostMapping("/expenses")
    public ResponseEntity<ExpenseDTO> createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        return ResponseEntity.ok(splitExpenseService.createExpense(request));
    }

    @GetMapping("/balances/{userId}")
    public ResponseEntity<List<BalanceDTO>> getBalancesForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(splitExpenseService.getBalancesForUser(userId));
    }

    @GetMapping("/groups/{groupId}/balances")
    public ResponseEntity<List<BalanceDTO>> getGroupBalances(@PathVariable Long groupId) {
        return ResponseEntity.ok(splitExpenseService.getGroupBalances(groupId));
    }

    @GetMapping("/settlements/minimum")
    public ResponseEntity<SettlementResponse> getMinimumSettlements() {
        return ResponseEntity.ok(splitExpenseService.getMinimumSettlements());
    }

    @PostMapping("/settlements")
    public ResponseEntity<Void> settleTransaction(@Valid @RequestBody SettlementRequest request) {
        splitExpenseService.settleTransaction(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/settlements/user/{userId}")
    public ResponseEntity<List<TransactionDTO>> getUserSettlements(@PathVariable Long userId) {
        return ResponseEntity.ok(splitExpenseService.getUserSettlements(userId));
    }


    @GetMapping("/groups/{groupId}/settlements/minimum")
    public ResponseEntity<SettlementResponse> getMinimumSettlementsForGroup(
            @PathVariable Long groupId) {
        return ResponseEntity.ok(
                splitExpenseService.getMinimumSettlementsForGroup(groupId)
        );
    }
}

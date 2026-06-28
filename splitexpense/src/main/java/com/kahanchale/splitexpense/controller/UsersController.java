package com.kahanchale.splitexpense.controller;

import com.kahanchale.splitexpense.dto.UserDTO;
import com.kahanchale.splitexpense.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/split-expense/users")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;

    /**
     * Get all users from travel_app excluding current user
     * @param currentUserId The current user ID (from JWT or request)
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getUsers(@RequestParam(required = false) Long currentUserId) {
        if (currentUserId == null) {
            return ResponseEntity.ok(userService.getAllUsers());
        }
        return ResponseEntity.ok(userService.getAllUsersExcludingCurrent(currentUserId));
    }

    /**
     * Get a specific user by ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
        UserDTO user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}

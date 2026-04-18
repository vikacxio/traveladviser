package com.kahanchale.traveladviser.controller;

import com.kahanchale.traveladviser.dto.LoginRequest;
import com.kahanchale.traveladviser.dto.LoginResponse;
import com.kahanchale.traveladviser.dto.RegisterRequest;
import com.kahanchale.traveladviser.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/success")
    public Map<String, String> success(@AuthenticationPrincipal OAuth2User user) {
        if(user==null) return null; //TODO
        if(user.getAttribute("email") == null || user.getAttribute("name")==null) return null;
        return Map.of(
                "message", "Login successful",
                "email", Objects.requireNonNull(user.getAttribute("email")),
                "name", Objects.requireNonNull(user.getAttribute("name"))
        );
    }
}
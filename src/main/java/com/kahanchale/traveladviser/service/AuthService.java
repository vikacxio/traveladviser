package com.kahanchale.traveladviser.service;

import com.kahanchale.traveladviser.request.LoginRequest;
import com.kahanchale.traveladviser.response.LoginResponse;
import com.kahanchale.traveladviser.request.RegisterRequest;
import com.kahanchale.traveladviser.entity.User;
import com.kahanchale.traveladviser.exception.InvalidCredentialsException;
import com.kahanchale.traveladviser.exception.ResourceAlreadyExistsException;
import com.kahanchale.traveladviser.exception.ResourceNotFoundException;
import com.kahanchale.traveladviser.repository.UserRepository;
import com.kahanchale.traveladviser.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public LoginResponse register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with email '" + request.getEmail() + "' already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProvider("LOCAL");

        User savedUser = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(savedUser.getEmail());

        return new LoginResponse(token, savedUser.getEmail(), savedUser.getName(), "User registered successfully");
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User with email '" + request.getEmail() + "' not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());

        return new LoginResponse(token, user.getEmail(), user.getName(), "Login successful");
    }
}
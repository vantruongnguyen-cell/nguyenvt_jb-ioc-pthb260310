package com.example.identityservice.controller;

import com.example.identityservice.dto.AuthResponse;
import com.example.identityservice.dto.LoginRequest;
import com.example.identityservice.dto.TokenRefreshRequest;
import com.example.identityservice.entity.RefreshToken;
import com.example.identityservice.entity.User;
import com.example.identityservice.repository.UserRepository;
import com.example.identityservice.service.RefreshTokenService;
import com.example.identityservice.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtils, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        String accessToken = jwtUtils.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken.getToken()));
    }

    // Endpoint cấp lại Token theo cơ chế Token Rotation
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        AuthResponse response = refreshTokenService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
}
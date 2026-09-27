package com.example.identityservice.service;

import com.example.identityservice.dto.LoginRequest;
import com.example.identityservice.dto.LoginResponse;
import com.example.identityservice.dto.RegisterRequest;
import com.example.identityservice.dto.UserResponse;
import com.example.identityservice.entity.User;
import com.example.identityservice.repository.UserRepository;
import com.example.identityservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : "USER")
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .role(savedUser.getRole())
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        // Tìm user theo username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Bad credentials"));

        // Kiểm tra password bằng BCrypt (matches)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Bad credentials");
        }

        // Sinh ra JWT token nếu hợp lệ
        String token = jwtUtil.generateAccessToken(user);
        return new LoginResponse(token);
    }

    public String generateTokenForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        return jwtUtil.generateAccessToken(user);
    }
}
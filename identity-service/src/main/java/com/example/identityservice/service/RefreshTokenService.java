package com.example.identityservice.service;

import com.example.identityservice.dto.AuthResponse;
import com.example.identityservice.entity.RefreshToken;
import com.example.identityservice.entity.User;
import com.example.identityservice.repository.RefreshTokenRepository;
import com.example.identityservice.repository.UserRepository;
import com.example.identityservice.util.JwtUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.jwt.refresh-token-expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtils; // 1. Bổ sung JwtUtils

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository,
                               JwtUtil jwtUtils) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken createRefreshToken(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return createRefreshToken(user);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    // --- Bổ sung cho Bài tập 3: Token Rotation ---
    @Transactional
    public AuthResponse refreshToken(String requestToken) {
        // 1. Tìm Refresh Token trong DB[cite: 2]
        RefreshToken token = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));

        // 2. Kiểm tra hết hạn[cite: 2]
        verifyExpiration(token);

        // 3. Lấy User sở hữu token[cite: 2]
        User user = token.getUser();

        // 4. (Rotation) Xóa Refresh Token cũ khỏi DB[cite: 2]
        refreshTokenRepository.delete(token);

        // 5. Cấp Access Token mới và Refresh Token mới[cite: 2]
        String newAccessToken = jwtUtils.generateAccessToken(user);
        RefreshToken newRefreshToken = createRefreshToken(user);

        // 6. Trả về cặp Token mới[cite: 2]
        return new AuthResponse(newAccessToken, newRefreshToken.getToken());
    }
}
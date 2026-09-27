package com.example.identityservice.dto;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
}
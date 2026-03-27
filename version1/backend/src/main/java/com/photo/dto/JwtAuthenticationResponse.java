package com.photo.dto;

import com.photo.entity.Photo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JwtAuthenticationResponse {
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserResponseDto user;

    public JwtAuthenticationResponse(String token, String refreshToken, Long expiresIn, UserResponseDto user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
}
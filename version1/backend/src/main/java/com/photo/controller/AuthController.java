package com.photo.controller;

import com.photo.dto.*;
import com.photo.entity.User;
import com.photo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final com.photo.config.JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            User user = userService.registerUser(registrationDto);

            // 创建用户响应DTO
            UserResponseDto userResponse = new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getCreatedAt(), // 使用真实的时间字段
                0
            );

            // 生成JWT令牌
            String token = jwtService.generateToken(user);
            String refreshToken = jwtService.generateToken(user); // 在实际应用中，刷新令牌应单独处理

            JwtAuthenticationResponse response = new JwtAuthenticationResponse(
                token,
                refreshToken,
                86400000L, // 24小时
                userResponse
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDto loginDto) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDto.getUsername(),
                    loginDto.getPassword()
                )
            );

            UserDetails userDetails = userService.loadUserByUsername(loginDto.getUsername());
            User user = userService.findByUsername(loginDto.getUsername());

            // 创建用户响应DTO
            UserResponseDto userResponse = new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getCreatedAt(), // 使用真实的时间字段
                user.getPhotos() != null ? user.getPhotos().size() : 0
            );

            // 生成JWT令牌
            String token = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateToken(userDetails); // 在实际应用中，刷新令牌应单独处理

            JwtAuthenticationResponse response = new JwtAuthenticationResponse(
                token,
                refreshToken,
                86400000L, // 24小时
                userResponse
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid username or password");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@RequestHeader("Authorization") String token) {
        String username = jwtService.extractUsername(token.substring(7)); // 移除 "Bearer " 前缀
        User user = userService.findByUsername(username);

        if (user != null) {
            UserResponseDto userResponse = new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getCreatedAt(), // 使用真实的时间字段
                user.getPhotos() != null ? user.getPhotos().size() : 0
            );
            return ResponseEntity.ok(userResponse);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String token,
                                          @RequestBody UserRegistrationDto profileDto) {
        String username = jwtService.extractUsername(token.substring(7)); // 移除 "Bearer " 前缀
        User existingUser = userService.findByUsername(username);

        if (existingUser != null) {
            existingUser.setFirstName(profileDto.getFirstName());
            existingUser.setLastName(profileDto.getLastName());
            existingUser.setBio(profileDto.getBio());

            User updatedUser = userService.findById(existingUser.getId());

            UserResponseDto userResponse = new UserResponseDto(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getBio(),
                updatedUser.getAvatarUrl(),
                updatedUser.getCreatedAt(), // 使用真实的时间字段
                updatedUser.getPhotos() != null ? updatedUser.getPhotos().size() : 0
            );

            return ResponseEntity.ok(userResponse);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
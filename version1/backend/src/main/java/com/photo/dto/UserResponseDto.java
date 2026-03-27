package com.photo.dto;

import com.photo.entity.Photo;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String bio;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private int totalPhotos;

    public UserResponseDto(Long id, String username, String email, String firstName, String lastName,
                          String bio, String avatarUrl, LocalDateTime createdAt, int totalPhotos) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.totalPhotos = totalPhotos;
    }
}
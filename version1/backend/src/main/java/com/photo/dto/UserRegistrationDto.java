package com.photo.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UserRegistrationDto {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String bio;
}
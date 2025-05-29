package com.example.demo_project.auth;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RegisterRequest {
    
    private String firstName;
    private String surName;
    private String email;
    private String departmentName;
    private String roleName;
    private Boolean enabled = false;
    private Boolean active = false;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}

package com.example.demo_project.auth;

import lombok.Data;

@Data
public class RegisterRequest {
    
    private String firstName;
    private String surName;
    private String email;
    private Integer departmentId;
    private String roleName;
}

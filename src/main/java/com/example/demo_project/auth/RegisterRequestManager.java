package com.example.demo_project.auth;

import lombok.Data;

@Data
public class RegisterRequestManager {
    private String firstName;
    private String surName;
    private String email;
    private String roleName;
}

package com.example.demo_project.auth;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String token;
    private String password;
    private String confirmPassword;
}

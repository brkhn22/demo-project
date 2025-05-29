package com.example.demo_project.auth.token;

import lombok.Data;

@Data
public class ConfirmRequest {
    private String token;
    private String email;
    private String password;
}

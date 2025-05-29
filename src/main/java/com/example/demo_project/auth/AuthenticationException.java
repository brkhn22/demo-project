package com.example.demo_project.auth;

public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super("Authentication Error: " + message);
    }

}

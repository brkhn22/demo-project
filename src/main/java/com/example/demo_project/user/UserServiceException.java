package com.example.demo_project.user;

public class UserServiceException extends RuntimeException {

    public UserServiceException(String message) {
        super("User service error: " + message);
    }


}

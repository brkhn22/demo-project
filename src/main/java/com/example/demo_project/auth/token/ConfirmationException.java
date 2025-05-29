package com.example.demo_project.auth.token;

public class ConfirmationException extends RuntimeException {

    public ConfirmationException(String message) {
        super("Confirmation Error: "+message);
    }

}

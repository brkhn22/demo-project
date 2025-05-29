package com.example.demo_project.user.company;

public class CompanyServiceException extends RuntimeException {

    public CompanyServiceException(String message) {
        super("Company Service Exception: "+message);
    }

}

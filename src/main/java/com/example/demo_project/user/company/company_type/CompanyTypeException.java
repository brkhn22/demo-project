package com.example.demo_project.user.company.company_type;

public class CompanyTypeException extends RuntimeException {

    public CompanyTypeException(String message) {
        super("Company Type Exception: " + message);
    }

}

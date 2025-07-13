package com.example.demo_project.auth;

public class Validator {

    public static void isValidName(String name) {
        if (name == null || name.isEmpty()) {
            throw new AuthenticationException("Invalid name format");
        }
    }

    public static void isValidRoleName(String roleName) {
        if (roleName == null || roleName.isEmpty()) {
            throw new AuthenticationException("Invalid role name format");
        }
    }

    public static void isValidDepartmentId(Integer departmentId) {
        // Check if the department ID is a positive integer
        if (departmentId == null || departmentId <= 0) {
            throw new AuthenticationException("Invalid department ID");
        }
    }

    public static void isValidEmail(String email){
        // Basic regex for validating an email address
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if(email == null || !email.matches(emailRegex)) {
            throw new AuthenticationException("Invalid email format");
        }
    }
    public static void isValidPassword(String password){
        String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$.!\\-+])[A-Za-z\\d@$.!\\-+]{8,32}$";
        if(password == null || !password.matches(regex))
            throw new AuthenticationException("Illegal password format");
    }
        
}

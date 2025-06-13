package com.example.demo_project.user.town;

public class TownServiceException extends RuntimeException {

    public TownServiceException(String message) {
        super("Town Service error: "+message);
    }

}

package com.pedrohenrique.pagcontrolback.exceptions;

public class UserIdRequiredException extends RuntimeException {
    public UserIdRequiredException(String message) {
        super(message);
    }
}

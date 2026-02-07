package com.pedrohenrique.pagcontrolback.exceptions;

public class UserRequiredException extends RuntimeException {
    public UserRequiredException(String message) {
        super(message);
    }
}

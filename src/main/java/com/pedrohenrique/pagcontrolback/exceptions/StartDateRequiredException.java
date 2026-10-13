package com.pedrohenrique.pagcontrolback.exceptions;

public class StartDateRequiredException extends RuntimeException {
    public StartDateRequiredException(String message) {
        super(message);
    }
}

package com.pedrohenrique.pagcontrolback.exceptions;

public class ExpiredTokenException extends RuntimeException {
    public ExpiredTokenException(String message) {
        super(message);
    }
}

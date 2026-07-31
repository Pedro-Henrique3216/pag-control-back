package com.pedrohenrique.pagcontrolback.exceptions;

public class ResendConfirmationLimitException extends RuntimeException {
    public ResendConfirmationLimitException(String message) {
        super(message);
    }
}

package com.pedrohenrique.pagcontrolback.exceptions;

public class InvalidCreditLimitException extends RuntimeException {
    public InvalidCreditLimitException(String message) {
        super(message);
    }
}

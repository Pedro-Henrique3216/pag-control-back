package com.pedrohenrique.pagcontrolback.exceptions;

public class CreditLimitExceededException extends RuntimeException {
    public CreditLimitExceededException(String message) {
        super(message);
    }
}

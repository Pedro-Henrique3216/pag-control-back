package com.pedrohenrique.pagcontrolback.exceptions;

public class CreditCardRequiredException extends RuntimeException {
    public CreditCardRequiredException(String message) {
        super(message);
    }
}

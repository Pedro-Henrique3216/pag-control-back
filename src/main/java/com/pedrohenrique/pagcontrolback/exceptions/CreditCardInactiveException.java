package com.pedrohenrique.pagcontrolback.exceptions;

public class CreditCardInactiveException extends RuntimeException {
    public CreditCardInactiveException(String message) {
        super(message);
    }
}

package com.pedrohenrique.pagcontrolback.exceptions;

public class CreditCardNotFoundException extends RuntimeException {
    public CreditCardNotFoundException(String message) {
        super(message);
    }
}

package com.pedrohenrique.pagcontrolback.exceptions;

public class InvalidCreditCardNameException extends RuntimeException {
    public InvalidCreditCardNameException(String message) {
        super(message);
    }
}

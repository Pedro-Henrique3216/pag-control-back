package com.pedrohenrique.pagcontrolback.exceptions;

public class InvalidCreditCardClosingDayException extends RuntimeException {
    public InvalidCreditCardClosingDayException(String message) {
        super(message);
    }
}

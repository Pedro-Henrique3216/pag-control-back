package com.pedrohenrique.pagcontrolback.exceptions;

public class InvalidPaymentTypeException extends RuntimeException {
    public InvalidPaymentTypeException(String message) {
        super(message);
    }
}

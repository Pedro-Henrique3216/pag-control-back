package com.pedrohenrique.pagcontrolback.exceptions;

public class PaymentTypeRequiredException extends RuntimeException {
    public PaymentTypeRequiredException(String message) {
        super(message);
    }
}

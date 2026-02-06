package com.pedrohenrique.pagcontrolback.exceptions;

public class InvalidInstallmentDueDateForPaymentTypeException extends RuntimeException {
    public InvalidInstallmentDueDateForPaymentTypeException(String message) {
        super(message);
    }
}

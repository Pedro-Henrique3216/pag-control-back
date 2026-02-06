package com.pedrohenrique.pagcontrolback.exceptions;

public class MultipleInstallmentsNotAllowedForPaymentTypeException extends RuntimeException {
    public MultipleInstallmentsNotAllowedForPaymentTypeException(String message) {
        super(message);
    }
}

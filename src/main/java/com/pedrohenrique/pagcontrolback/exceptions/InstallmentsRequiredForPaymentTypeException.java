package com.pedrohenrique.pagcontrolback.exceptions;

public class InstallmentsRequiredForPaymentTypeException extends RuntimeException {
    public InstallmentsRequiredForPaymentTypeException(String message) {
        super(message);
    }
}

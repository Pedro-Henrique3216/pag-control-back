package com.pedrohenrique.pagcontrolback.exceptions;

public class InvalidInstallmentDueInDaysException extends RuntimeException {
    public InvalidInstallmentDueInDaysException(String message) {
        super(message);
    }
}

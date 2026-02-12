package com.pedrohenrique.pagcontrolback.exceptions;

public class InstallmentAlreadyPaidException extends RuntimeException {
    public InstallmentAlreadyPaidException(String message) {
        super(message);
    }
}

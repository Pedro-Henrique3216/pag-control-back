package com.pedrohenrique.pagcontrolback.exceptions;

public class InstallmentAccessDeniedException extends RuntimeException {
    public InstallmentAccessDeniedException(String message) {
        super(message);
    }
}

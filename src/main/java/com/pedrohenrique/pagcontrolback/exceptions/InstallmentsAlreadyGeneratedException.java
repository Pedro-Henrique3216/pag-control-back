package com.pedrohenrique.pagcontrolback.exceptions;

public class InstallmentsAlreadyGeneratedException extends RuntimeException {
    public InstallmentsAlreadyGeneratedException(String message) {
        super(message);
    }
}

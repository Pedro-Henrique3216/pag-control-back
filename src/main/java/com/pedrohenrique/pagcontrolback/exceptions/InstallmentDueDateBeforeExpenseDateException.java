package com.pedrohenrique.pagcontrolback.exceptions;

public class InstallmentDueDateBeforeExpenseDateException extends RuntimeException {
    public InstallmentDueDateBeforeExpenseDateException(String message) {
        super(message);
    }
}

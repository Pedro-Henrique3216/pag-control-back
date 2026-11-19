package com.pedrohenrique.pagcontrolback.exceptions;

public class RecurringExpenseNotFoundException extends RuntimeException {
    public RecurringExpenseNotFoundException(String message) {
        super(message);
    }
}

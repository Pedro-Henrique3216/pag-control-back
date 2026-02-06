package com.pedrohenrique.pagcontrolback.exceptions;

public class ExpenseRequiredException extends RuntimeException {
    public ExpenseRequiredException(String message) {
        super(message);
    }
}

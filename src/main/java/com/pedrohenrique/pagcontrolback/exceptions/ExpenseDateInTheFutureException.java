package com.pedrohenrique.pagcontrolback.exceptions;

public class ExpenseDateInTheFutureException extends RuntimeException {
    public ExpenseDateInTheFutureException(String message) {
        super(message);
    }
}

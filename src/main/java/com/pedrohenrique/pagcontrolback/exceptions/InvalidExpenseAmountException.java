package com.pedrohenrique.pagcontrolback.exceptions;

public class InvalidExpenseAmountException extends RuntimeException {
    public InvalidExpenseAmountException(String message) {
        super(message);
    }
}

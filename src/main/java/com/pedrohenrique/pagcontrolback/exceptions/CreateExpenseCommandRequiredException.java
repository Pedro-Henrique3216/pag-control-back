package com.pedrohenrique.pagcontrolback.exceptions;

public class CreateExpenseCommandRequiredException extends RuntimeException {
    public CreateExpenseCommandRequiredException(String message)
    {
        super(message);
    }
}

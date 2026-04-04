package com.pedrohenrique.pagcontrolback.exceptions;

public class CreateIncomeCommandRequiredException extends RuntimeException {
    public CreateIncomeCommandRequiredException(String message) {
        super(message);
    }
}

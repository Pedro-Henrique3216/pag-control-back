package com.pedrohenrique.pagcontrolback.exceptions;

public class FutureMonthNotAllowedException extends RuntimeException {
    public FutureMonthNotAllowedException(String message) {
        super(message);
    }
}

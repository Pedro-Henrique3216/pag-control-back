package com.pedrohenrique.pagcontrolback.exceptions;

public class InvalidConfirmationTokenException extends RuntimeException {
    public InvalidConfirmationTokenException(String message) {
        super(message);
    }
}

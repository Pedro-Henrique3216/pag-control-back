package com.pedrohenrique.pagcontrolback.exceptions;

public class CreateSupplierCommandRequiredException extends RuntimeException {
    public CreateSupplierCommandRequiredException(String message) {
        super(message);
    }
}

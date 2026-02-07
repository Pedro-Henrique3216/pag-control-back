package com.pedrohenrique.pagcontrolback.exceptions;

public class SupplierAlreadyExistsWithCnpjException extends RuntimeException {
    public SupplierAlreadyExistsWithCnpjException(String message) {
        super(message);
    }
}

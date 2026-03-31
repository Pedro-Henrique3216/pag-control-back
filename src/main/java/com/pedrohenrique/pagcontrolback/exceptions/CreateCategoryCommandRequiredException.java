package com.pedrohenrique.pagcontrolback.exceptions;

public class CreateCategoryCommandRequiredException extends RuntimeException {
    public CreateCategoryCommandRequiredException(String message) {
        super(message);
    }
}

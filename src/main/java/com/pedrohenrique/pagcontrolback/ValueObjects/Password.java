package com.pedrohenrique.pagcontrolback.ValueObjects;

import com.pedrohenrique.pagcontrolback.exceptions.UserDomainException;

import java.util.Objects;

public record Password(String value) {

    public Password {
        if (value == null || value.isBlank()) {
            throw new UserDomainException("Password is required");
        }

        if (value.length() < 8) {
            throw new UserDomainException("Password must have at least 8 characters");
        }

        if (!value.matches(".*\\d.*")) {
            throw new UserDomainException("Password must contain a number");
        }

        if (!value.matches(".*[A-Z].*")) {
            throw new UserDomainException("Password must contain an uppercase letter");
        }

        if (!value.matches(".*[a-z].*")) {
            throw new UserDomainException("Password must contain a lowercase letter");
        }

        if (!value.matches(".*[^a-zA-Z0-9].*")) {
            throw new UserDomainException("Password must contain a special character");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Password password)) return false;
        return Objects.equals(value, password.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}

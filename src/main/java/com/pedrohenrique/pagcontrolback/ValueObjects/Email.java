package com.pedrohenrique.pagcontrolback.ValueObjects;

import com.pedrohenrique.pagcontrolback.exceptions.UserDomainException;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record Email(String value) {

    public Email(String value) {

        if (value == null || value.trim().isEmpty()) {
            throw new UserDomainException("Email cannot be null or empty");
        }

        if (!value.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new UserDomainException("Email format is invalid");
        }

        this.value = value.toLowerCase().trim();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Email email)) return false;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}

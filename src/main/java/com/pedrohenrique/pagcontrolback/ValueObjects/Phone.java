package com.pedrohenrique.pagcontrolback.ValueObjects;

import com.pedrohenrique.pagcontrolback.exceptions.UserDomainException;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record Phone(String value) {

    public Phone(String value) {

        if (value == null || value.trim().isEmpty()) {
            throw new UserDomainException("Phone cannot be null or empty");
        }

        String normalized = value.replaceAll("\\D", "");

        if (normalized.length() != 11) {
            throw new UserDomainException("Phone must be 11 digits");
        }

        this.value = normalized;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Phone phone)) return false;
        return Objects.equals(value, phone.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}

package com.pedrohenrique.pagcontrolback.ValueObjects;

import com.pedrohenrique.pagcontrolback.exceptions.InvalidSupplierCnpjException;
import com.pedrohenrique.pagcontrolback.utils.ValidateCnpj;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record Cnpj(String value) {

    public Cnpj(String value) {

        String normalized = value.replaceAll("\\D", "");

        if (!ValidateCnpj.isValidCnpj(normalized)) {
            throw new InvalidSupplierCnpjException("Invalid CNPJ");
        }

        this.value = normalized;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cnpj cnpj)) return false;
        return Objects.equals(value, cnpj.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}

package com.pedrohenrique.pagcontrolback.ValueObjects;

import com.pedrohenrique.pagcontrolback.exceptions.InvalidSupplierCnpjException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CnpjTest {

    @Nested
    class Success {

        @Test
        void shouldCreateCnpjWhenValidCnpjIsProvided() {
            Cnpj cnpj = new Cnpj("12345678000195");

            assertEquals("12345678000195", cnpj.value());
        }

        @Test
        void shouldNormalizeCnpjByRemovingNonDigitCharacters() {
            Cnpj cnpj = new Cnpj("12.345.678/0001-95");

            assertEquals("12345678000195", cnpj.value());
        }
    }

    @Nested
    class Errors {

        @Test
        void shouldThrowExceptionWhenInvalidCnpjIsProvided() {
            assertThrows(
                    InvalidSupplierCnpjException.class,
                    () -> new Cnpj("12345678000196")
            );
        }
    }
}
package com.pedrohenrique.pagcontrolback.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidateCnpjTest {

    @Nested
    class Success {

        @Test
        void shouldReturnTrueWhenCnpjIsValid() {
            assertTrue(ValidateCnpj.isValidCnpj("12345678000195"));
        }

        @Test
        void shouldReturnTrueWhenAnotherValidCnpjIsProvided() {
            assertTrue(ValidateCnpj.isValidCnpj("11222333000181"));
        }
    }

    @Nested
    class Errors {

        @Test
        void shouldReturnFalseWhenCheckDigitsAreInvalid() {
            assertFalse(ValidateCnpj.isValidCnpj("12345678000196"));
        }

        @Test
        void shouldReturnFalseWhenCnpjIsTooShort() {
            assertFalse(ValidateCnpj.isValidCnpj("1234567800019"));
        }

        @Test
        void shouldReturnFalseWhenCnpjIsTooLong() {
            assertFalse(ValidateCnpj.isValidCnpj("123456780001950"));
        }

        @Test
        void shouldReturnFalseWhenCnpjContainsNonDigitCharacters() {
            assertFalse(ValidateCnpj.isValidCnpj("12345678A00195"));
        }

        @Test
        void shouldReturnFalseWhenCnpjIsNull() {
            assertFalse(ValidateCnpj.isValidCnpj(null));
        }
    }
}
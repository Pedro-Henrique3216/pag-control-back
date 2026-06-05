package com.pedrohenrique.pagcontrolback.ValueObjects;

import com.pedrohenrique.pagcontrolback.exceptions.UserDomainException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordTest {

    @Nested
    class Success {

        @Test
        void shouldCreatePasswordWhenValidPasswordIsProvided() {
            Password password = new Password("12345678Ab@");

            assertEquals("12345678Ab@", password.value());
        }
    }

    @Nested
    class Errors {

        @Test
        void shouldThrowExceptionWhenPasswordIsNull() {
            assertThrows(
                    UserDomainException.class,
                    () -> new Password(null)
            );
        }

        @Test
        void shouldThrowExceptionWhenPasswordIsBlank() {
            assertThrows(
                    UserDomainException.class,
                    () -> new Password("")
            );
        }

        @Test
        void shouldThrowExceptionWhenPasswordDoesNotContainUppercaseLetter() {
            assertThrows(
                    UserDomainException.class,
                    () -> new Password("12345678ab@")
            );
        }

        @Test
        void shouldThrowExceptionWhenPasswordDoesNotContainLowercaseLetter() {
            assertThrows(
                    UserDomainException.class,
                    () -> new Password("12345678AB@")
            );
        }

        @Test
        void shouldThrowExceptionWhenPasswordDoesNotContainDigit() {
            assertThrows(
                    UserDomainException.class,
                    () -> new Password("Password@")
            );
        }

        @Test
        void shouldThrowExceptionWhenPasswordDoesNotContainSpecialCharacter() {
            assertThrows(
                    UserDomainException.class,
                    () -> new Password("12345678Ab")
            );
        }

        @Test
        void shouldThrowExceptionWhenPasswordIsTooShort() {
            assertThrows(
                    UserDomainException.class,
                    () -> new Password("1Ab@")
            );
        }
    }
}
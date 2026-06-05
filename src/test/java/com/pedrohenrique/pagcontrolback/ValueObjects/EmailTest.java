package com.pedrohenrique.pagcontrolback.ValueObjects;

import com.pedrohenrique.pagcontrolback.exceptions.UserDomainException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailTest {

    @Nested
    class Success {

        @Test
        void shouldCreateEmailWhenValidEmailIsProvided() {
            Email email = new Email("teste@gmail.com");

            assertEquals("teste@gmail.com", email.value());
        }

        @Test
        void shouldNormalizeEmailToLowerCase() {
            Email email = new Email("Teste@GMAIL.com");

            assertEquals("teste@gmail.com", email.value());
        }
    }

    @Nested
    class Errors {

        @Test
        void shouldThrowExceptionWhenEmailIsInvalid() {
            assertThrows(
                    UserDomainException.class,
                    () -> new Email("email-invalido")
            );
        }

        @Test
        void shouldThrowExceptionWhenEmailIsNull() {
            assertThrows(
                    UserDomainException.class,
                    () -> new Email(null)
            );
        }

        @Test
        void shouldThrowExceptionWhenEmailIsBlank() {
            assertThrows(
                    UserDomainException.class,
                    () -> new Email("")
            );
        }
    }
}
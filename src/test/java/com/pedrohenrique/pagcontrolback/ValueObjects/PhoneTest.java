package com.pedrohenrique.pagcontrolback.ValueObjects;

import com.pedrohenrique.pagcontrolback.exceptions.UserDomainException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneTest {

    @Nested
    class Success {

        @Test
        void shouldCreatePhoneWhenValidPhoneIsProvided() {
            Phone phone = new Phone("(11)91234-5678");

            assertEquals("11912345678", phone.value());
        }

        @Test
        void shouldNormalizePhoneByRemovingNonDigitCharacters() {
            Phone phone = new Phone("(11)92222-3333");

            assertEquals("11922223333", phone.value());
        }
    }

    @Nested
    class Errors {

        @Test
        void shouldThrowExceptionWhenPhoneIsNull() {
            assertThrows(
                    UserDomainException.class,
                    () -> new Phone(null)
            );
        }

        @Test
        void shouldThrowExceptionWhenPhoneIsBlank() {
            assertThrows(
                    UserDomainException.class,
                    () -> new Phone("")
            );
        }

        @Test
        void shouldThrowExceptionWhenPhoneHasInvalidLength() {
            assertThrows(
                    UserDomainException.class,
                    () -> new Phone("12345")
            );
        }

        @Test
        void shouldThrowExceptionWhenPhoneContainsLetters() {
            assertThrows(
                    UserDomainException.class,
                    () -> new Phone("(11)9A234-5678")
            );
        }
    }
}
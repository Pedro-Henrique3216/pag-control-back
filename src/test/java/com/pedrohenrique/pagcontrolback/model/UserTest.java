package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.exceptions.UserDomainException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Nested
    class ConstructorTests {

        @Test
        void shouldCreatePfUserSuccessfully() {
            User user = new User(
                    "John Doe",
                    null,
                    "test@gmail.com",
                    "12345678",
                    "11999999999",
                    PersonType.PF
            );

            assertEquals("John Doe", user.getName());
            assertEquals(PersonType.PF, user.getPersonType());
            assertNull(user.getFantasyName());
            assertEquals("test@gmail.com", user.getEmail().value());
            assertEquals("11999999999", user.getPhone().value());
            assertNotNull(user.getCreatedAt());
        }

        @Test
        void shouldCreatePjUserSuccessfully() {
            User user = new User(
                    "Empresa LTDA",
                    "Minha Empresa",
                    "empresa@gmail.com",
                    "12345678",
                    "11999999999",
                    PersonType.PJ
            );

            assertEquals("Empresa LTDA", user.getName());
            assertEquals(PersonType.PJ, user.getPersonType());
            assertEquals("Minha Empresa", user.getFantasyName());
            assertEquals("empresa@gmail.com", user.getEmail().value());
            assertEquals("11999999999", user.getPhone().value());
        }

        @Test
        void shouldThrowWhenNameIsNull() {
            assertThrows(
                    UserDomainException.class,
                    () -> new User(
                            null,
                            null,
                            "test@gmail.com",
                            "12345678",
                            "11999999999",
                            PersonType.PF
                    )
            );
        }

        @Test
        void shouldThrowWhenNameIsBlank() {
            assertThrows(
                    UserDomainException.class,
                    () -> new User(
                            "   ",
                            null,
                            "test@gmail.com",
                            "12345678",
                            "11999999999",
                            PersonType.PF
                    )
            );
        }

        @Test
        void shouldThrowWhenPasswordIsNull() {
            assertThrows(
                    UserDomainException.class,
                    () -> new User(
                            "John Doe",
                            null,
                            "test@gmail.com",
                            null,
                            "11999999999",
                            PersonType.PF
                    )
            );
        }

        @Test
        void shouldThrowWhenPasswordIsBlank() {
            assertThrows(
                    UserDomainException.class,
                    () -> new User(
                            "John Doe",
                            null,
                            "test@gmail.com",
                            "   ",
                            "11999999999",
                            PersonType.PF
                    )
            );
        }

        @Test
        void shouldThrowWhenPersonTypeIsNull() {
            assertThrows(
                    UserDomainException.class,
                    () -> new User(
                            "John Doe",
                            null,
                            "test@gmail.com",
                            "12345678",
                            "11999999999",
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenPfHasFantasyName() {
            assertThrows(
                    UserDomainException.class,
                    () -> new User(
                            "John Doe",
                            "Minha Empresa",
                            "test@gmail.com",
                            "12345678",
                            "11999999999",
                            PersonType.PF
                    )
            );
        }

        @Test
        void shouldThrowWhenPjHasNoFantasyName() {
            assertThrows(
                    UserDomainException.class,
                    () -> new User(
                            "Empresa LTDA",
                            null,
                            "empresa@gmail.com",
                            "12345678",
                            "11999999999",
                            PersonType.PJ
                    )
            );
        }

        @Test
        void shouldThrowWhenPjHasBlankFantasyName() {
            assertThrows(
                    UserDomainException.class,
                    () -> new User(
                            "Empresa LTDA",
                            "   ",
                            "empresa@gmail.com",
                            "12345678",
                            "11999999999",
                            PersonType.PJ
                    )
            );
        }
    }

    @Nested
    class PasswordTests {

        @Test
        void shouldUpdatePassword() {
            User user = new User(
                    "John Doe",
                    null,
                    "test@gmail.com",
                    "12345678",
                    "11999999999",
                    PersonType.PF
            );

            user.setPassword("87654321");

            assertEquals("87654321", user.getPassword());
        }
    }

    @Nested
    class ResendEmailTests {

        @Test
        void shouldAllowResendConfirmationEmail() {
            User user = new User(
                    "John Doe",
                    null,
                    "test@gmail.com",
                    "12345678",
                    "11999999999",
                    PersonType.PF
            );

            assertTrue(user.canResendConfirmationEmail(Clock.systemDefaultZone()));
        }

        @Test
        void shouldBlockAfterThreeAttempts(){
            User user = new User(
                    "John Doe",
                    null,
                    "test@gmail.com",
                    "12345678",
                    "11999999999",
                    PersonType.PF
            );
            user.registerConfirmationEmailSent(Clock.systemDefaultZone());
            user.registerConfirmationEmailSent(Clock.systemDefaultZone());
            user.registerConfirmationEmailSent(Clock.systemDefaultZone());
            assertFalse(user.canResendConfirmationEmail(Clock.systemDefaultZone()));
        }

        @Test
        void shouldAllowAgainAfterTenMinutes(){
            User user = new User(
                    "John Doe",
                    null,
                    "test@gmail.com",
                    "12345678",
                    "11999999999",
                    PersonType.PF
            );

            Clock clock = Clock.fixed(
                    Instant.parse("2026-07-31T10:00:00Z"),
                    ZoneOffset.UTC
            );

            user.registerConfirmationEmailSent(clock);
            user.registerConfirmationEmailSent(clock);
            user.registerConfirmationEmailSent(clock);

            Clock afterTenMinutes = Clock.fixed(
                    Instant.parse("2026-07-31T10:11:00Z"),
                    ZoneOffset.UTC
            );

            assertTrue(user.canResendConfirmationEmail(afterTenMinutes));

        }

        @Test
        void shouldNotAllowWhenEmailIsVerified(){
            User user = new User(
                    "John Doe",
                    null,
                    "test@gmail.com",
                    "12345678",
                    "11999999999",
                    PersonType.PF
            );
            user.verifyEmail();
            assertFalse(user.canResendConfirmationEmail(Clock.systemDefaultZone()));
        }

    }
}
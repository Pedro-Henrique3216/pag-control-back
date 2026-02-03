package com.pedrohenrique.pagcontrolback.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void whenPersonTypesIsPJAndFieldsCorrect_shouldCreateUserSuccessfully() {
        assertDoesNotThrow(
                () -> new User(
                        "John Doe",
                        "JD Supplies",
                        "test@gmail.com",
                        "12345678",
                        "111-222-3333",
                        PersonType.PJ
                )
        );
    }

    @Test
    void whenPersonTypesIsPFAndFieldsCorrect_shouldCreateUserSuccessfully() {
        assertDoesNotThrow(
                () -> new User(
                        "John Doe",
                        null,
                        "test@gmail.com",
                        "12345678",
                        "111-222-3333",
                        PersonType.PF
                )
        );
    }



    @Test
    void whenPersonTypeIsNull_shouldThrowUserDomainException() {
        assertThrows(
                com.pedrohenrique.pagcontrolback.exceptions.UserDomainException.class,
                () -> new User(
                        "John Doe",
                        "JD Supplies",
                        "test@gmail.com",
                        "12345678",
                        "111-222-3333",
                        null
                )
        );
    }

    @Test
    void whenCreatingUserWithPersonTypeISPFAndNameFantasyNotNull_shouldThrowUserDomainException() {
        assertThrows(
                com.pedrohenrique.pagcontrolback.exceptions.UserDomainException.class,
                () -> new User(
                        "John Doe",
                        "JD Supplies",
                        "test@gmail.com",
                        "12345678",
                        "111-222-3333",
                        PersonType.PF
                )
        );
    }

    @Test
    void whenCreatingUserWithPersonTypeIsPJAndNoFantasyName_shouldThrowUserDomainException() {
        assertThrows(
                com.pedrohenrique.pagcontrolback.exceptions.UserDomainException.class,
                () -> new User(
                        "John Doe",
                        null,
                        "test@gmail.com",
                        "12345678",
                        "111-222-3333",
                        PersonType.PJ
                )
        );
    }


}
package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.exceptions.CreditLimitExceededException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CreditCardTest {

    private User createUser() {
        return new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "$2a$10$abcdefghijklmnopqrstuv",
                "11999999999",
                PersonType.PF
        );
    }

    @Test
    void shouldCreateCreditCardSuccessfully() {
        User user = createUser();
        Money creditLimit = new Money(new BigDecimal("5000.00"));

        CreditCard creditCard = new CreditCard(
                "Nubank",
                10,
                17,
                creditLimit,
                user
        );

        assertEquals("Nubank", creditCard.getName());
        assertEquals(10, creditCard.getClosingDay());
        assertEquals(17, creditCard.getDueDay());
        assertEquals(creditLimit.value(), creditCard.getCreditLimit().value());
        assertEquals(user, creditCard.getUser());
    }

    @Test
    void shouldCreateCreditCardAsActive() {
        CreditCard creditCard = new CreditCard(
                "Nubank",
                10,
                17,
                new Money(new BigDecimal("5000.00")),
                createUser()
        );

        assertTrue(creditCard.isActive());
    }

    @Test
    void shouldReturnCreditLimit() {
        Money creditLimit = new Money(new BigDecimal("5000.00"));

        CreditCard creditCard = new CreditCard(
                "Nubank",
                10,
                17,
                creditLimit,
                createUser()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                creditCard.getCreditLimit().value()
        );
    }

    @Test
    void shouldNotThrowWhenExpenseIsBelowCreditLimit() {
        CreditCard creditCard = new CreditCard(
                "Nubank",
                10,
                17,
                new Money(new BigDecimal("5000.00")),
                createUser()
        );

        Money expenseValue = new Money(new BigDecimal("1000.00"));

        assertDoesNotThrow(() -> creditCard.hasLimit(expenseValue));
    }

    @Test
    void shouldNotThrowWhenExpenseIsEqualToCreditLimit() {
        CreditCard creditCard = new CreditCard(
                "Nubank",
                10,
                17,
                new Money(new BigDecimal("5000.00")),
                createUser()
        );

        Money expenseValue = new Money(new BigDecimal("5000.00"));

        assertDoesNotThrow(() -> creditCard.hasLimit(expenseValue));
    }

    @Test
    void shouldThrowWhenExpenseExceedsCreditLimit() {
        CreditCard creditCard = new CreditCard(
                "Nubank",
                10,
                17,
                new Money(new BigDecimal("5000.00")),
                createUser()
        );

        Money expenseValue = new Money(new BigDecimal("5000.01"));

        CreditLimitExceededException exception = assertThrows(
                CreditLimitExceededException.class,
                () -> creditCard.hasLimit(expenseValue)
        );

        assertEquals("credit limit exceeded", exception.getMessage());
    }

    @Test
    void shouldReturnAssociatedUser() {
        User user = createUser();

        CreditCard creditCard = new CreditCard(
                "Nubank",
                10,
                17,
                new Money(new BigDecimal("5000.00")),
                user
        );

        assertEquals(user, creditCard.getUser());
    }

    @Test
    void shouldReturnClosingAndDueDays() {
        CreditCard creditCard = new CreditCard(
                "Nubank",
                10,
                17,
                new Money(new BigDecimal("5000.00")),
                createUser()
        );

        assertEquals(10, creditCard.getClosingDay());
        assertEquals(17, creditCard.getDueDay());
    }
}
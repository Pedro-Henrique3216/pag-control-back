package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.exceptions.*;
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

    private CreditCard createCreditCard() {
        return new CreditCard(
                "Nubank",
                10,
                17,
                new Money(new BigDecimal("2000.00")),
                createUser()
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
        assertEquals(
                creditLimit.value(),
                creditCard.getCreditLimit().value()
        );
        assertEquals(user, creditCard.getUser());
    }

    @Test
    void shouldCreateCreditCardAsActive() {
        CreditCard creditCard = createCreditCard();

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
        CreditCard creditCard = createCreditCard();

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
        CreditCard creditCard = createCreditCard();

        assertEquals(10, creditCard.getClosingDay());
        assertEquals(17, creditCard.getDueDay());
    }

    @Test
    void shouldThrowWhenNameIsNull() {
        InvalidCreditCardNameException exception = assertThrows(
                InvalidCreditCardNameException.class,
                () -> new CreditCard(
                        null,
                        10,
                        17,
                        new Money(new BigDecimal("5000.00")),
                        createUser()
                )
        );

        assertEquals(
                "Credit card name cannot be null or empty",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenNameIsEmpty() {
        InvalidCreditCardNameException exception = assertThrows(
                InvalidCreditCardNameException.class,
                () -> new CreditCard(
                        "",
                        10,
                        17,
                        new Money(new BigDecimal("5000.00")),
                        createUser()
                )
        );

        assertEquals(
                "Credit card name cannot be null or empty",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenNameContainsOnlyWhitespace() {
        assertThrows(
                InvalidCreditCardNameException.class,
                () -> new CreditCard(
                        "   ",
                        10,
                        17,
                        new Money(new BigDecimal("5000.00")),
                        createUser()
                )
        );
    }

    @Test
    void shouldThrowWhenClosingDayIsLessThanOne() {
        assertThrows(
                InvalidCreditCardClosingDayException.class,
                () -> new CreditCard(
                        "Nubank",
                        0,
                        17,
                        new Money(new BigDecimal("5000.00")),
                        createUser()
                )
        );
    }

    @Test
    void shouldThrowWhenClosingDayIsGreaterThanThirtyOne() {
        assertThrows(
                InvalidCreditCardClosingDayException.class,
                () -> new CreditCard(
                        "Nubank",
                        32,
                        17,
                        new Money(new BigDecimal("5000.00")),
                        createUser()
                )
        );
    }

    @Test
    void shouldThrowWhenDueDayIsLessThanOne() {
        assertThrows(
                InvalidCreditCardDueDayException.class,
                () -> new CreditCard(
                        "Nubank",
                        10,
                        0,
                        new Money(new BigDecimal("5000.00")),
                        createUser()
                )
        );
    }

    @Test
    void shouldThrowWhenDueDayIsGreaterThanThirtyOne() {
        assertThrows(
                InvalidCreditCardDueDayException.class,
                () -> new CreditCard(
                        "Nubank",
                        10,
                        32,
                        new Money(new BigDecimal("5000.00")),
                        createUser()
                )
        );
    }

    @Test
    void shouldThrowWhenCreditLimitIsNull() {
        assertThrows(
                InvalidCreditLimitException.class,
                () -> new CreditCard(
                        "Nubank",
                        10,
                        17,
                        null,
                        createUser()
                )
        );
    }

    // Tests for updateCreditCard

    @Test
    void shouldUpdateCreditCardInformation() {
        CreditCard creditCard = createCreditCard();

        BigDecimal newCreditLimit = new BigDecimal("5000.00");

        creditCard.updateCreditCard(
                "Inter",
                15,
                25,
                newCreditLimit
        );

        assertEquals("Inter", creditCard.getName());
        assertEquals(15, creditCard.getClosingDay());
        assertEquals(25, creditCard.getDueDay());
        assertEquals(
                newCreditLimit,
                creditCard.getCreditLimit().value()
        );
    }

    @Test
    void shouldUpdateOnlyNameWhenOtherFieldsAreNull() {
        CreditCard creditCard = createCreditCard();

        Integer previousClosingDay = creditCard.getClosingDay();
        Integer previousDueDay = creditCard.getDueDay();
        BigDecimal previousCreditLimit =
                creditCard.getCreditLimit().value();

        creditCard.updateCreditCard("Inter", null, null, null);

        assertEquals("Inter", creditCard.getName());
        assertEquals(previousClosingDay, creditCard.getClosingDay());
        assertEquals(previousDueDay, creditCard.getDueDay());
        assertEquals(
                previousCreditLimit,
                creditCard.getCreditLimit().value()
        );
    }

    @Test
    void shouldUpdateOnlyClosingDay() {
        CreditCard creditCard = createCreditCard();

        creditCard.updateCreditCard(null, 15, null, null);

        assertEquals(15, creditCard.getClosingDay());
        assertEquals("Nubank", creditCard.getName());
        assertEquals(17, creditCard.getDueDay());
        assertEquals(
                new BigDecimal("2000.00"),
                creditCard.getCreditLimit().value()
        );
    }

    @Test
    void shouldUpdateOnlyDueDay() {
        CreditCard creditCard = createCreditCard();

        creditCard.updateCreditCard(null, null, 25, null);

        assertEquals(25, creditCard.getDueDay());
        assertEquals("Nubank", creditCard.getName());
        assertEquals(10, creditCard.getClosingDay());
        assertEquals(
                new BigDecimal("2000.00"),
                creditCard.getCreditLimit().value()
        );
    }

    @Test
    void shouldUpdateOnlyCreditLimit() {
        CreditCard creditCard = createCreditCard();

        BigDecimal newCreditLimit = new BigDecimal("5000.00");

        creditCard.updateCreditCard(
                null,
                null,
                null,
                newCreditLimit
        );

        assertEquals(
                newCreditLimit,
                creditCard.getCreditLimit().value()
        );
        assertEquals("Nubank", creditCard.getName());
        assertEquals(10, creditCard.getClosingDay());
        assertEquals(17, creditCard.getDueDay());
    }

    @Test
    void shouldKeepAllFieldsWhenUpdateValuesAreNull() {
        CreditCard creditCard = createCreditCard();

        String previousName = creditCard.getName();
        Integer previousClosingDay = creditCard.getClosingDay();
        Integer previousDueDay = creditCard.getDueDay();
        BigDecimal previousCreditLimit =
                creditCard.getCreditLimit().value();

        creditCard.updateCreditCard(null, null, null, null);

        assertEquals(previousName, creditCard.getName());
        assertEquals(previousClosingDay, creditCard.getClosingDay());
        assertEquals(previousDueDay, creditCard.getDueDay());
        assertEquals(
                previousCreditLimit,
                creditCard.getCreditLimit().value()
        );
    }

    @Test
    void shouldKeepCreditCardActiveAfterUpdate() {
        CreditCard creditCard = createCreditCard();

        creditCard.updateCreditCard(
                "Inter",
                15,
                25,
                new BigDecimal("5000.00")
        );

        assertTrue(creditCard.isActive());
    }

    @Test
    void shouldKeepAssociatedUserAfterUpdate() {
        User user = createUser();

        CreditCard creditCard = new CreditCard(
                "Nubank",
                10,
                17,
                new Money(new BigDecimal("2000.00")),
                user
        );

        creditCard.updateCreditCard(
                "Inter",
                15,
                25,
                new BigDecimal("5000.00")
        );

        assertEquals(user, creditCard.getUser());
    }

    @Test
    void shouldThrowWhenUpdatedNameIsNull() {
        CreditCard creditCard = createCreditCard();

        assertThrows(
                InvalidCreditCardNameException.class,
                () -> creditCard.updateCreditCard(
                        "",
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void shouldThrowWhenUpdatedNameContainsOnlyWhitespace() {
        CreditCard creditCard = createCreditCard();

        assertThrows(
                InvalidCreditCardNameException.class,
                () -> creditCard.updateCreditCard("   ", null, null, null)
        );
    }

    @Test
    void shouldThrowWhenUpdatedClosingDayIsInvalid() {
        CreditCard creditCard = createCreditCard();

        assertThrows(
                InvalidCreditCardClosingDayException.class,
                () -> creditCard.updateCreditCard(null, 0, null, null)
        );
    }

    @Test
    void shouldThrowWhenUpdatedClosingDayIsGreaterThanThirtyOne() {
        CreditCard creditCard = createCreditCard();

        assertThrows(
                InvalidCreditCardClosingDayException.class,
                () -> creditCard.updateCreditCard(null, 32, null, null)
        );
    }

    @Test
    void shouldThrowWhenUpdatedDueDayIsInvalid() {
        CreditCard creditCard = createCreditCard();

        assertThrows(
                InvalidCreditCardDueDayException.class,
                () -> creditCard.updateCreditCard(null, null, 0, null)
        );
    }

    @Test
    void shouldThrowWhenUpdatedDueDayIsGreaterThanThirtyOne() {
        CreditCard creditCard = createCreditCard();

        assertThrows(
                InvalidCreditCardDueDayException.class,
                () -> creditCard.updateCreditCard(null, null, 32, null)
        );
    }

    @Test
    void shouldDeactivateCreditCardSuccessfully() {
        CreditCard creditCard = createCreditCard();

        creditCard.deactivateCreditCard();

        assertFalse(creditCard.isActive());
    }
}
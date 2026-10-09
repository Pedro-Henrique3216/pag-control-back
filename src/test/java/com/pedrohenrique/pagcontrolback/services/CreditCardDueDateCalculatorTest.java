package com.pedrohenrique.pagcontrolback.services;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CreditCardDueDateCalculatorTest {

    @Test
    void shouldReturnDueDateInCurrentMonthWhenExpenseIsBeforeClosingDay() {
        LocalDate calculateCreditDueDate = CreditCardDueDateCalculator.calculateDueDate(LocalDate.of(2026, 9, 5), 10, 17, 1);
        assertEquals(LocalDate.of(2026, 9, 17), calculateCreditDueDate);
    }

    @Test
    void shouldReturnDueDateInNextMonthWhenExpenseIsAfterClosingDay() {
        LocalDate calculateCreditDueDate = CreditCardDueDateCalculator.calculateDueDate(LocalDate.of(2026, 9, 12), 10, 17, 1);
        assertEquals(LocalDate.of(2026, 10, 17), calculateCreditDueDate);
    }

    @Test
    void shouldReturnDueDateInNextMonthWhenOneInstallmentIsAdded() {
        LocalDate calculateCreditDueDate = CreditCardDueDateCalculator.calculateDueDate(LocalDate.of(2026, 9, 5), 10, 17, 2);
        assertEquals(LocalDate.of(2026, 10, 17), calculateCreditDueDate);
    }

    @Test
    void shouldReturnDueDateTwoMonthsAheadWhenThreeInstallmentsAreAdded() {
        LocalDate calculateCreditDueDate = CreditCardDueDateCalculator.calculateDueDate(LocalDate.of(2026, 9, 5), 10, 17, 3);
        assertEquals(LocalDate.of(2026, 11, 17), calculateCreditDueDate);
    }

    @Test
    void shouldReturnDueDateInCurrentMonthWhenExpenseIsOnClosingDay() {
        LocalDate calculateCreditDueDate = CreditCardDueDateCalculator.calculateDueDate(LocalDate.of(2026, 9, 10), 10, 17, 1);
        assertEquals(LocalDate.of(2026, 9, 17), calculateCreditDueDate);
    }

}
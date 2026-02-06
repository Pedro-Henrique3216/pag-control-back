package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.exceptions.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseTest {

    @Test
    void whenExpenseDateIsNull_thenThrowExpenseDateRequiredException(){
        Exception exception = assertThrows(ExpenseDateRequiredException.class, () -> {
            new Expense("INV123", PaymentType.CASH, null);
        });
        String expectedMessage = "Expense date is required.";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenExpenseDateIsInFuture_thenThrowExpenseDateInTheFutureException(){
        Exception exception = assertThrows(ExpenseDateInTheFutureException.class, () -> {
            new Expense("INV123", PaymentType.CASH, LocalDate.now().plusDays(1));
        });
        String expectedMessage = "Expense date cannot be in the future.";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenAddNullInstallment_thenThrowInstallmentRequiredException(){
        Expense expense = new Expense("INV123", PaymentType.CASH, LocalDate.now());
        Exception exception = assertThrows(InstallmentRequiredException.class, () -> {
            expense.addInstallment(null);
        });
        String expectedMessage = "Installment cannot be null.";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenPaymentTypeIsCashAndAddMoreThanOneInstallment_thenThrowMultipleInstallmentsNotAllowedForPaymentTypeException(){
        Expense expense = new Expense("INV123", PaymentType.CASH, LocalDate.now());
        Installment installment = new Installment(BigDecimal.valueOf(100.00), LocalDate.now(), null);
        Installment installment2 = new Installment(BigDecimal.valueOf(100.00), LocalDate.now().plusDays(60), null);
        expense.addInstallment(installment);
        Exception exception = assertThrows(MultipleInstallmentsNotAllowedForPaymentTypeException.class, () -> {
            expense.addInstallment(installment2);
        });
        String expectedMessage = "Payment type " + PaymentType.CASH + " allows only one installment";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenPaymentTypeIsCashAndInstallmentDueDateIsDifferentFromExpenseDate_thenThrowInvalidInstallmentDueDateForPaymentTypeException(){
        Expense expense = new Expense("INV123", PaymentType.CASH, LocalDate.now());
        Installment installment = new Installment(BigDecimal.valueOf(100.00), LocalDate.now().plusDays(30), null);
        Exception exception = assertThrows(InvalidInstallmentDueDateForPaymentTypeException.class, () -> {
            expense.addInstallment(installment);
        });
        String expectedMessage = "For payment type "+ PaymentType.CASH + ", the installment due date must be the same as the expense date";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenInstallmentDueDateIsBeforeExpenseDate_thenThrowInstallmentDueDateBeforeExpenseDateException(){
        Expense expense = new Expense("INV123", PaymentType.CREDIT, LocalDate.now());
        Installment installment = new Installment(BigDecimal.valueOf(100.00), LocalDate.now().minusDays(1), null);
        Exception exception = assertThrows(InstallmentDueDateBeforeExpenseDateException.class, () -> {
            expense.addInstallment(installment);
        });
        String expectedMessage = "Installment due date cannot be before the expense date.";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenPaymentTypeIsCashAndInstallmentIsValid_thenAddInstallment(){
        Expense expense = new Expense("INV123", PaymentType.CASH, LocalDate.now());
        Installment installment = new Installment(BigDecimal.valueOf(100.00), LocalDate.now(), null);
        expense.addInstallment(installment);
        assertEquals(1, expense.getInstallments().size());
        assertEquals(installment, expense.getInstallments().get(0));
    }

    @Test
    void whenPaymentTypeIsCredit_thenAllowMultipleInstallments(){
        Expense expense = new Expense("INV123", PaymentType.CREDIT, LocalDate.now());
        Installment installment1 = new Installment(BigDecimal.valueOf(100.00), LocalDate.now().plusDays(30), null);
        Installment installment2 = new Installment(BigDecimal.valueOf(100.00), LocalDate.now().plusDays(60), null);
        expense.addInstallment(installment1);
        expense.addInstallment(installment2);
        assertEquals(2, expense.getInstallments().size());
        assertEquals(installment1, expense.getInstallments().get(0));
        assertEquals(installment2, expense.getInstallments().get(1));
    }

}
package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.exceptions.InstallmentAlreadyPaidException;
import com.pedrohenrique.pagcontrolback.exceptions.InstallmentDueDateRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.InvalidInstallmentAmountException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InstallmentTest {

    @Test
    void shouldCreateInstallmentWhenDataIsValid() {

        Installment installment = new Installment(
                new BigDecimal("100.00"),
                LocalDate.now(),
                "123456"
        );

        assertNotNull(installment);
        assertEquals(new BigDecimal("100.00"), installment.getAmount());
        assertEquals(InstallmentStatus.UNPAID, installment.getStatus());
        assertEquals("123456", installment.getBarcode());
        assertNotNull(installment.getDueDate());
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNull() {

        assertThrows(
                InvalidInstallmentAmountException.class,
                () -> new Installment(
                        null,
                        LocalDate.now(),
                        "123"
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenAmountIsZero() {

        assertThrows(
                InvalidInstallmentAmountException.class,
                () -> new Installment(
                        BigDecimal.ZERO,
                        LocalDate.now(),
                        "123"
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNegative() {

        assertThrows(
                InvalidInstallmentAmountException.class,
                () -> new Installment(
                        new BigDecimal("-10"),
                        LocalDate.now(),
                        "123"
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenDueDateIsNull() {

        assertThrows(
                InstallmentDueDateRequiredException.class,
                () -> new Installment(
                        new BigDecimal("10"),
                        null,
                        "123"
                )
        );
    }

    @Test
    void shouldMarkInstallmentAsPaid() {
        Installment installment = new Installment(
                new BigDecimal("100.00"),
                LocalDate.now(),
                "123456"
        );

        installment.markAsPaid();

        assertEquals(InstallmentStatus.PAID, installment.getStatus());
        assertNotNull(installment.getPaymentDate());
    }

    @Test
    void shouldMarkInstallmentAsPaidWhenExpensePaymentTypeIsCash() {

        Installment installment = new Installment(
                new BigDecimal("100.00"),
                LocalDate.now(),
                "123"
        );

        Expense expense = new Expense(
                "123467897",
                PaymentType.CASH,
                LocalDate.now(),
                new User(),
                new Supplier()
        );

        installment.setExpense(expense);

        assertEquals(InstallmentStatus.PAID, installment.getStatus());
        assertNotNull(installment.getPaymentDate());
    }

    @Test
    void shouldMarkInstallmentAsPaidWhenExpensePaymentTypeIsDebit() {

        Installment installment = new Installment(
                new BigDecimal("100.00"),
                LocalDate.now(),
                "123"
        );

        Expense expense = new Expense(
                "123467897",
                PaymentType.DEBIT,
                LocalDate.now(),
                new User(),
                new Supplier()
        );

        installment.setExpense(expense);

        assertEquals(InstallmentStatus.PAID, installment.getStatus());
        assertNotNull(installment.getPaymentDate());
    }

    @Test
    void shouldNotMarkInstallmentAsPaidWhenExpensePaymentTypeIsNotCashOrDebit() {

        Installment installment = new Installment(
                new BigDecimal("100.00"),
                LocalDate.now(),
                "123"
        );

        Expense expense = new Expense(
                "123467897",
                PaymentType.CREDIT,
                LocalDate.now(),
                new User(),
                new Supplier()
        );

        installment.setExpense(expense);

        assertEquals(InstallmentStatus.UNPAID, installment.getStatus());
        assertNull(installment.getPaymentDate());
    }

    @Test
    void shouldThrowInstallmentAlreadyPaidExceptionWhenInstallmentIsAlreadyPaid() {

        Installment installment = new Installment(
                new BigDecimal("100.00"),
                LocalDate.now(),
                "123"
        );

        installment.markAsPaid();

        assertThrows(
                InstallmentAlreadyPaidException.class,
                installment::markAsPaid
        );
    }

    @Test
    void shouldThrowInstallmentAlreadyPaidExceptionWhenUpdatingPaidInstallment(){
        Installment installment = new Installment(
                new BigDecimal("100.00"),
                LocalDate.now(),
                "123"
        );

        installment.markAsPaid();

        assertThrows(
                InstallmentAlreadyPaidException.class,
                () -> installment.updateInstallment(new BigDecimal("150.00"), LocalDate.now().plusDays(10), "456")
        );
    }

    @Test
    void shouldUpdateAmountWhenAmountIsProvided(){
        Installment installment = new Installment(
                new BigDecimal("100.00"),
                LocalDate.now(),
                "123"
        );

        installment.updateInstallment(new BigDecimal("150.00"), null, null);

        assertEquals(new BigDecimal("150.00"), installment.getAmount());
    }

    @Test
    void shouldUpdateDueDateWhenDueDateIsProvided(){
        Installment installment = new Installment(
                new BigDecimal("100.00"),
                LocalDate.now(),
                "123"
        );

        LocalDate newDueDate = LocalDate.now().plusDays(10);
        installment.updateInstallment(null, newDueDate, null);

        assertEquals(newDueDate, installment.getDueDate());
    }

    @Test
    void shouldUpdateBarcodeWhenBarcodeIsProvided(){
        Installment installment = new Installment(
                new BigDecimal("100.00"),
                LocalDate.now(),
                "123"
        );

        String newBarcode = "456";
        installment.updateInstallment(null, null, newBarcode);

        assertEquals(newBarcode, installment.getBarcode());
    }

    @Test
    void shouldUpdateAllFieldsWhenAllParametersAreProvided(){
        Installment installment = new Installment(
                new BigDecimal("100.00"),
                LocalDate.now(),
                "123"
        );

        LocalDate newDueDate = LocalDate.now().plusDays(10);
        String newBarcode = "456";
        installment.updateInstallment(new BigDecimal("150.00"), newDueDate, newBarcode);

        assertEquals(new BigDecimal("150.00"), installment.getAmount());
        assertEquals(newDueDate, installment.getDueDate());
        assertEquals(newBarcode, installment.getBarcode());
    }

    @Test
    void shouldNotChangeAnythingWhenAllParametersAreNull(){
        Installment installment = new Installment(
                new BigDecimal("100.00"),
                LocalDate.now(),
                "123"
        );

        BigDecimal originalAmount = installment.getAmount();
        LocalDate originalDueDate = installment.getDueDate();
        String originalBarcode = installment.getBarcode();

        installment.updateInstallment(null, null, null);

        assertEquals(originalAmount, installment.getAmount());
        assertEquals(originalDueDate, installment.getDueDate());
        assertEquals(originalBarcode, installment.getBarcode());
    }

    @Test
    void shouldThrowExceptionWhenAmountIsInvalid(){
        Installment installment = new Installment(
                new BigDecimal("100.00"),
                LocalDate.now(),
                "123"
        );

        assertThrows(
                InvalidInstallmentAmountException.class,
                () -> installment.updateInstallment(new BigDecimal("-10"), null, null)
        );
    }

}
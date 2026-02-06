package com.pedrohenrique.pagcontrolback.model;

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
}
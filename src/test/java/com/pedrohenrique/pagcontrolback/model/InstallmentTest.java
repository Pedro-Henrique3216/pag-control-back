package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.exceptions.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InstallmentTest {

    private Installment createInstallment() {
        return new Installment(
                Money.of(new BigDecimal("100.00")),
                LocalDate.now(),
                "123456",
                null,
                1,
                1
        );
    }

    @Nested
    class ConstructorTests {

        @Test
        void shouldCreateInstallmentSuccessfully() {
            Installment installment = createInstallment();

            assertEquals(
                    Money.of(new BigDecimal("100.00")),
                    installment.getAmount()
            );

            assertEquals(
                    InstallmentStatus.UNPAID,
                    installment.getStatus()
            );

            assertEquals("123456", installment.getBarcode());
        }

        @Test
        void shouldThrowWhenAmountIsNull() {
            assertThrows(
                    InvalidInstallmentAmountException.class,
                    () -> new Installment(
                            null,
                            LocalDate.now(),
                            null,
                            null,
                            1,
                            1
                    )
            );
        }

        @Test
        void shouldThrowWhenDueDateIsNull() {
            assertThrows(
                    InstallmentDueDateRequiredException.class,
                    () -> new Installment(
                            Money.of(BigDecimal.TEN),
                            null,
                            null,
                            null,
                            1,
                            1
                    )
            );
        }

        @Test
        void shouldThrowWhenInstallmentNumberIsNull() {
            assertThrows(
                    InvalidInstallmentNumberException.class,
                    () -> new Installment(
                            Money.of(BigDecimal.TEN),
                            LocalDate.now(),
                            null,
                            null,
                            null,
                            1
                    )
            );
        }

        @Test
        void shouldThrowWhenTotalInstallmentsIsNull() {
            assertThrows(
                    InvalidTotalInstallmentsException.class,
                    () -> new Installment(
                            Money.of(BigDecimal.TEN),
                            LocalDate.now(),
                            null,
                            null,
                            1,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenInstallmentNumberIsZero() {
            assertThrows(
                    InvalidInstallmentNumberException.class,
                    () -> new Installment(
                            Money.of(BigDecimal.TEN),
                            LocalDate.now(),
                            null,
                            null,
                            0,
                            1
                    )
            );
        }

        @Test
        void shouldThrowWhenTotalInstallmentsIsZero() {
            assertThrows(
                    InvalidTotalInstallmentsException.class,
                    () -> new Installment(
                            Money.of(BigDecimal.TEN),
                            LocalDate.now(),
                            null,
                            null,
                            1,
                            0
                    )
            );
        }

        @Test
        void shouldThrowWhenInstallmentNumberGreaterThanTotal() {
            assertThrows(
                    InvalidInstallmentNumberException.class,
                    () -> new Installment(
                            Money.of(BigDecimal.TEN),
                            LocalDate.now(),
                            null,
                            null,
                            5,
                            3
                    )
            );
        }
    }

    @Nested
    class MarkAsPaidTests {

        @Test
        void shouldMarkInstallmentAsPaid() {
            Installment installment = createInstallment();

            installment.markAsPaid();

            assertEquals(
                    InstallmentStatus.PAID,
                    installment.getStatus()
            );

            assertNotNull(
                    installment.getPaymentDate()
            );
        }

        @Test
        void shouldThrowWhenInstallmentAlreadyPaid() {
            Installment installment = createInstallment();

            installment.markAsPaid();

            assertThrows(
                    InstallmentAlreadyPaidException.class,
                    installment::markAsPaid
            );
        }
    }

    @Nested
    class UpdateInstallmentTests {

        @Test
        void shouldUpdateAmount() {
            Installment installment = createInstallment();

            installment.updateInstallment(
                    new BigDecimal("150.00"),
                    null,
                    null
            );

            assertEquals(
                    Money.of(new BigDecimal("150.00")),
                    installment.getAmount()
            );
        }

        @Test
        void shouldUpdateDueDate() {
            Installment installment = createInstallment();

            LocalDate newDate = LocalDate.now().plusDays(10);

            installment.updateInstallment(
                    null,
                    newDate,
                    null
            );

            assertEquals(
                    newDate,
                    installment.getDueDate()
            );
        }

        @Test
        void shouldUpdateBarcode() {
            Installment installment = createInstallment();

            installment.updateInstallment(
                    null,
                    null,
                    "999999"
            );

            assertEquals(
                    "999999",
                    installment.getBarcode()
            );
        }

        @Test
        void shouldUpdateAllFields() {
            Installment installment = createInstallment();

            LocalDate newDate = LocalDate.now().plusDays(5);

            installment.updateInstallment(
                    new BigDecimal("200.00"),
                    newDate,
                    "888888"
            );

            assertEquals(
                    Money.of(new BigDecimal("200.00")),
                    installment.getAmount()
            );

            assertEquals(
                    newDate,
                    installment.getDueDate()
            );

            assertEquals(
                    "888888",
                    installment.getBarcode()
            );
        }

        @Test
        void shouldNotChangeAnythingWhenAllParametersAreNull() {
            Installment installment = createInstallment();

            Money originalAmount = installment.getAmount();
            LocalDate originalDueDate = installment.getDueDate();
            String originalBarcode = installment.getBarcode();

            installment.updateInstallment(
                    null,
                    null,
                    null
            );

            assertEquals(originalAmount, installment.getAmount());
            assertEquals(originalDueDate, installment.getDueDate());
            assertEquals(originalBarcode, installment.getBarcode());
        }

        @Test
        void shouldThrowWhenUpdatingPaidInstallment() {
            Installment installment = createInstallment();

            installment.markAsPaid();

            assertThrows(
                    InstallmentAlreadyPaidException.class,
                    () -> installment.updateInstallment(
                            new BigDecimal("150.00"),
                            LocalDate.now().plusDays(10),
                            "123"
                    )
            );
        }
    }
}
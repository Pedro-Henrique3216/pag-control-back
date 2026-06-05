package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.exceptions.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseTest {

    private Expense createExpense(PaymentType paymentType) {
        return new Expense(
                "INV123",
                "Compra mercado",
                paymentType,
                LocalDate.now(),
                new User(),
                Money.of(BigDecimal.valueOf(100))
        );
    }

    private Installment createInstallment(
            Expense expense,
            LocalDate dueDate
    ) {
        return new Installment(
                Money.of(BigDecimal.valueOf(100)),
                dueDate,
                null,
                expense,
                1,
                1
        );
    }

    @Nested
    class ConstructorTests {

        @Test
        void shouldThrowWhenExpenseDateIsNull() {
            assertThrows(
                    ExpenseDateRequiredException.class,
                    () -> new Expense(
                            "INV123",
                            "Compra",
                            PaymentType.CASH,
                            null,
                            new User(),
                            Money.of(BigDecimal.valueOf(100))
                    )
            );
        }

        @Test
        void shouldThrowWhenExpenseDateIsFuture() {
            assertThrows(
                    ExpenseDateInTheFutureException.class,
                    () -> new Expense(
                            "INV123",
                            "Compra",
                            PaymentType.CASH,
                            LocalDate.now().plusDays(1),
                            new User(),
                            Money.of(BigDecimal.valueOf(100))
                    )
            );
        }

        @Test
        void shouldThrowWhenDescriptionIsNull() {
            assertThrows(
                    DescriptionRequiredException.class,
                    () -> new Expense(
                            "INV123",
                            null,
                            PaymentType.CASH,
                            LocalDate.now(),
                            new User(),
                            Money.of(BigDecimal.valueOf(100))
                    )
            );
        }
    }

    @Nested
    class AddInstallmentTests {

        @Test
        void shouldThrowWhenInstallmentIsNull() {
            Expense expense = createExpense(PaymentType.CASH);

            assertThrows(
                    InstallmentRequiredException.class,
                    () -> expense.addInstallment(null)
            );
        }

        @Test
        void shouldThrowWhenCashHasMoreThanOneInstallment() {
            Expense expense = createExpense(PaymentType.CASH);

            Installment installment1 =
                    createInstallment(expense, LocalDate.now());

            Installment installment2 =
                    new Installment(
                            Money.of(BigDecimal.valueOf(100)),
                            LocalDate.now(),
                            null,
                            expense,
                            2,
                            2
                    );

            expense.addInstallment(installment1);

            assertThrows(
                    MultipleInstallmentsNotAllowedForPaymentTypeException.class,
                    () -> expense.addInstallment(installment2)
            );
        }

        @Test
        void shouldThrowWhenCashDueDateDiffersFromExpenseDate() {
            Expense expense = createExpense(PaymentType.CASH);

            Installment installment =
                    createInstallment(
                            expense,
                            LocalDate.now().plusDays(30)
                    );

            assertThrows(
                    InvalidInstallmentDueDateForPaymentTypeException.class,
                    () -> expense.addInstallment(installment)
            );
        }

        @Test
        void shouldThrowWhenDueDateBeforeExpenseDate() {
            Expense expense = createExpense(PaymentType.CREDIT);

            Installment installment =
                    createInstallment(
                            expense,
                            LocalDate.now().minusDays(1)
                    );

            assertThrows(
                    InstallmentDueDateBeforeExpenseDateException.class,
                    () -> expense.addInstallment(installment)
            );
        }

        @Test
        void shouldAddInstallmentForCash() {
            Expense expense = createExpense(PaymentType.CASH);

            Installment installment =
                    createInstallment(expense, LocalDate.now());

            expense.addInstallment(installment);

            assertEquals(1, expense.getInstallments().size());
        }

        @Test
        void shouldAllowMultipleInstallmentsForCredit() {
            Expense expense = createExpense(PaymentType.CREDIT);

            Installment installment1 =
                    new Installment(
                            Money.of(BigDecimal.valueOf(50)),
                            LocalDate.now().plusDays(30),
                            null,
                            expense,
                            1,
                            2
                    );

            Installment installment2 =
                    new Installment(
                            Money.of(BigDecimal.valueOf(50)),
                            LocalDate.now().plusDays(60),
                            null,
                            expense,
                            2,
                            2
                    );

            expense.addInstallment(installment1);
            expense.addInstallment(installment2);

            assertEquals(2, expense.getInstallments().size());
        }
    }

    @Nested
    class GenerateInstallmentsTests {

        @Test
        void shouldGenerateSingleInstallmentForCash() {
            Expense expense = createExpense(
                    PaymentType.CASH
            );

            expense.generateInstallments(null);

            assertEquals(1, expense.getInstallments().size());

            Installment installment = expense.getInstallments().get(0);

            assertEquals(Money.of(BigDecimal.valueOf(100)), installment.getAmount());
            assertEquals(InstallmentStatus.PAID, installment.getStatus());
            assertEquals(1, installment.getInstallmentNumber());
            assertEquals(1, installment.getTotalInstallments());
        }

        @Test
        void shouldGenerateThreeInstallmentsForCredit() {
            Expense expense = createExpense(
                    PaymentType.CREDIT
            );

            Map<Integer, String> installments = new HashMap<>();
            installments.put(30, null);
            installments.put(60, null);
            installments.put(90, null);

            expense.generateInstallments(installments);

            assertEquals(3, expense.getInstallments().size());

            assertEquals(
                    Money.of(BigDecimal.valueOf(33.33)),
                    expense.getInstallments().get(0).getAmount()
            );

            assertEquals(
                    Money.of(BigDecimal.valueOf(33.33)),
                    expense.getInstallments().get(1).getAmount()
            );

            assertEquals(
                    Money.of(BigDecimal.valueOf(33.34)),
                    expense.getInstallments().get(2).getAmount()
            );
        }

        @Test
        void shouldDistributeRemainderToLastInstallment() {
            Expense expense = createExpense(
                    PaymentType.CREDIT
            );

            Map<Integer, String> installments = new HashMap<>();
            installments.put(30, null);
            installments.put(60, null);
            installments.put(90, null);

            expense.generateInstallments(installments);

            assertEquals(
                    Money.of(BigDecimal.valueOf(33.33)),
                    expense.getInstallments().get(0).getAmount()
            );

            assertEquals(
                    Money.of(BigDecimal.valueOf(33.33)),
                    expense.getInstallments().get(1).getAmount()
            );

            assertEquals(
                    Money.of(BigDecimal.valueOf(33.34)),
                    expense.getInstallments().get(2).getAmount()
            );
        }

        @Test
        void shouldThrowWhenCreditInstallmentsAreNotProvided() {
            Expense expense = createExpense(
                    PaymentType.CREDIT
            );

            assertThrows(
                    InstallmentsRequiredForPaymentTypeException.class,
                    () -> expense.generateInstallments(null)
            );
        }

        @Test
        void shouldThrowWhenDueInDaysIsZeroForCredit() {
            Expense expense = createExpense(
                    PaymentType.CREDIT
            );

            Map<Integer, String> installments = new HashMap<>();
            installments.put(0, null);


            assertThrows(
                    InvalidInstallmentDueInDaysException.class,
                    () -> expense.generateInstallments(installments)
            );
        }

        @Test
        void shouldThrowWhenDueInDaysIsNegative() {
            Expense expense = createExpense(
                    PaymentType.CREDIT
            );

            Map<Integer, String> installments = new HashMap<>();
            installments.put(-30, null);

            assertThrows(
                    InvalidInstallmentDueInDaysException.class,
                    () -> expense.generateInstallments(installments)
            );
        }

        @Test
        void shouldThrowWhenGeneratingInstallmentsTwice() {
            Expense expense = createExpense(
                    PaymentType.CREDIT
            );

            Map<Integer, String> installments = new HashMap<>();
            installments.put(30, null);

            expense.generateInstallments(installments);

            assertThrows(
                    InstallmentsAlreadyGeneratedException.class,
                    () -> expense.generateInstallments(installments)
            );
        }

        @Test
        void shouldThrowWhenCashReceivesMoreThanOneInstallment() {
            Expense expense = createExpense(
                    PaymentType.CASH
            );

            Map<Integer, String> installments = new HashMap<>();
            installments.put(0, null);
            installments.put(30, null);

            assertThrows(
                    MultipleInstallmentsNotAllowedForPaymentTypeException.class,
                    () -> expense.generateInstallments(installments)
            );
        }

        @Test
        void shouldThrowWhenCashInstallmentIsNotZeroDays() {
            Expense expense = createExpense(
                    PaymentType.CASH
            );

            Map<Integer, String> installments = new HashMap<>();
            installments.put(30, null);

            assertThrows(
                    InvalidInstallmentDueInDaysException.class,
                    () -> expense.generateInstallments(installments)
            );
        }
    }
}
package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.exceptions.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecurringExpenseTest {

    private final LocalDate startDate = LocalDate.of(2026, 1, 10);
    private final Money amount = Money.of(new BigDecimal("100.00"));

    private RecurringExpense createRecurringExpense(
            PaymentType paymentType,
            RecurrenceType recurrenceType,
            Integer recurrenceInterval,
            Integer recurrenceDay,
            LocalDate startDate,
            LocalDate recurrenceEndDate,
            CreditCard creditCard
    ) {
        return new RecurringExpense(
                "Monthly expense",
                amount,
                paymentType,
                recurrenceType,
                recurrenceInterval,
                recurrenceDay,
                startDate,
                recurrenceEndDate,
                new User(),
                null,
                null,
                creditCard
        );
    }

    private RecurringExpense createMonthlyExpense() {
        return createRecurringExpense(
                PaymentType.PIX,
                RecurrenceType.MONTHLY,
                1,
                10,
                startDate,
                null,
                null
        );
    }

    @Nested
    class ConstructorTests {

        @Test
        void shouldCreateRecurringExpenseSuccessfully() {
            RecurringExpense expense = createMonthlyExpense();

            assertEquals("Monthly expense", expense.getDescription());
            assertEquals(amount, expense.getAmount());
            assertEquals(PaymentType.PIX, expense.getPaymentType());
            assertEquals(RecurrenceType.MONTHLY, expense.getRecurrenceType());
            assertEquals(1, expense.getRecurrenceInterval());
            assertEquals(10, expense.getRecurrenceDay());
            assertEquals(startDate, expense.getStartDate());
            assertEquals(startDate, expense.getNextOccurrence());
            assertTrue(expense.isActive());
            assertNotNull(expense.getUser());
        }

        @Test
        void shouldCreateRecurringExpenseWithCreditCard() {
            CreditCard creditCard = mock(CreditCard.class);

            RecurringExpense expense = createRecurringExpense(
                    PaymentType.CREDIT,
                    RecurrenceType.MONTHLY,
                    1,
                    10,
                    startDate,
                    null,
                    creditCard
            );

            assertEquals(creditCard, expense.getCreditCard());

            verify(creditCard).hasLimit(amount);
        }

        @Test
        void shouldNotAssignCreditCardWhenPaymentTypeIsNotCredit() {
            CreditCard creditCard = mock(CreditCard.class);

            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.MONTHLY,
                    1,
                    10,
                    startDate,
                    null,
                    creditCard
            );

            assertNull(expense.getCreditCard());

            verifyNoInteractions(creditCard);
        }

        @Test
        void shouldThrowWhenAmountIsNull() {
            assertThrows(
                    InvalidAmountException.class,
                    () -> new RecurringExpense(
                            "Monthly expense",
                            null,
                            PaymentType.PIX,
                            RecurrenceType.MONTHLY,
                            1,
                            10,
                            startDate,
                            null,
                            new User(),
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenDescriptionIsNull() {
            assertThrows(
                    DescriptionRequiredException.class,
                    () -> new RecurringExpense(
                            null,
                            amount,
                            PaymentType.PIX,
                            RecurrenceType.MONTHLY,
                            1,
                            10,
                            startDate,
                            null,
                            new User(),
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenDescriptionIsBlank() {
            assertThrows(
                    DescriptionRequiredException.class,
                    () -> new RecurringExpense(
                            "   ",
                            amount,
                            PaymentType.PIX,
                            RecurrenceType.MONTHLY,
                            1,
                            10,
                            startDate,
                            null,
                            new User(),
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenPaymentTypeIsNull() {
            assertThrows(
                    PaymentTypeRequiredException.class,
                    () -> new RecurringExpense(
                            "Monthly expense",
                            amount,
                            null,
                            RecurrenceType.MONTHLY,
                            1,
                            10,
                            startDate,
                            null,
                            new User(),
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenRecurrenceTypeIsNull() {
            assertThrows(
                    RecurrenceTypeRequiredException.class,
                    () -> new RecurringExpense(
                            "Monthly expense",
                            amount,
                            PaymentType.PIX,
                            null,
                            1,
                            10,
                            startDate,
                            null,
                            new User(),
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenRecurrenceIntervalIsNull() {
            assertThrows(
                    RecurrenceIntervalException.class,
                    () -> new RecurringExpense(
                            "Monthly expense",
                            amount,
                            PaymentType.PIX,
                            RecurrenceType.MONTHLY,
                            null,
                            10,
                            startDate,
                            null,
                            new User(),
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenRecurrenceIntervalIsZero() {
            assertThrows(
                    RecurrenceIntervalException.class,
                    () -> new RecurringExpense(
                            "Monthly expense",
                            amount,
                            PaymentType.PIX,
                            RecurrenceType.MONTHLY,
                            0,
                            10,
                            startDate,
                            null,
                            new User(),
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenRecurrenceIntervalIsNegative() {
            assertThrows(
                    RecurrenceIntervalException.class,
                    () -> new RecurringExpense(
                            "Monthly expense",
                            amount,
                            PaymentType.PIX,
                            RecurrenceType.MONTHLY,
                            -1,
                            10,
                            startDate,
                            null,
                            new User(),
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenStartDateIsNull() {
            assertThrows(
                    StartDateRequiredException.class,
                    () -> new RecurringExpense(
                            "Monthly expense",
                            amount,
                            PaymentType.PIX,
                            RecurrenceType.MONTHLY,
                            1,
                            10,
                            null,
                            null,
                            new User(),
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenMonthlyRecurrenceDayIsNull() {
            assertThrows(
                    RecurrenceDayException.class,
                    () -> new RecurringExpense(
                            "Monthly expense",
                            amount,
                            PaymentType.PIX,
                            RecurrenceType.MONTHLY,
                            1,
                            null,
                            startDate,
                            null,
                            new User(),
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenMonthlyRecurrenceDayIsLessThanOne() {
            assertThrows(
                    RecurrenceDayException.class,
                    () -> new RecurringExpense(
                            "Monthly expense",
                            amount,
                            PaymentType.PIX,
                            RecurrenceType.MONTHLY,
                            1,
                            0,
                            startDate,
                            null,
                            new User(),
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenMonthlyRecurrenceDayIsGreaterThanThirtyOne() {
            assertThrows(
                    RecurrenceDayException.class,
                    () -> new RecurringExpense(
                            "Monthly expense",
                            amount,
                            PaymentType.PIX,
                            RecurrenceType.MONTHLY,
                            1,
                            32,
                            startDate,
                            null,
                            new User(),
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldAllowRecurrenceDayNullForNonMonthlyRecurrence() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.DAILY,
                    1,
                    null,
                    startDate,
                    null,
                    null
            );

            assertNull(expense.getRecurrenceDay());
            assertEquals(startDate, expense.getNextOccurrence());
        }

        @Test
        void shouldThrowWhenRecurrenceEndDateIsBeforeStartDate() {
            LocalDate endDate = startDate.minusDays(1);

            assertThrows(
                    InvalidRecurrenceEndDateException.class,
                    () -> createRecurringExpense(
                            PaymentType.PIX,
                            RecurrenceType.MONTHLY,
                            1,
                            10,
                            startDate,
                            endDate,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenCreditCardIsRequired() {
            assertThrows(
                    CreditCardRequiredException.class,
                    () -> createRecurringExpense(
                            PaymentType.CREDIT,
                            RecurrenceType.MONTHLY,
                            1,
                            10,
                            startDate,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenCreditCardLimitIsExceeded() {
            CreditCard creditCard = mock(CreditCard.class);

            doThrow(new CreditLimitExceededException("Credit limit exceeded"))
                    .when(creditCard)
                    .hasLimit(amount);

            assertThrows(
                    CreditLimitExceededException.class,
                    () -> createRecurringExpense(
                            PaymentType.CREDIT,
                            RecurrenceType.MONTHLY,
                            1,
                            10,
                            startDate,
                            null,
                            creditCard
                    )
            );
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void shouldUpdateRecurringExpenseSuccessfully() {
            RecurringExpense expense = createMonthlyExpense();

            String newDescription = "Updated expense";
            Money newAmount = Money.of(new BigDecimal("200.00"));

            expense.update(
                    newDescription,
                    newAmount,
                    PaymentType.PIX,
                    null,
                    null,
                    null
            );

            assertEquals(newDescription, expense.getDescription());
            assertEquals(newAmount, expense.getAmount());
            assertEquals(PaymentType.PIX, expense.getPaymentType());
            assertNull(expense.getCreditCard());
        }

        @Test
        void shouldUpdateRecurringExpenseWithSupplierAndCategory() {
            RecurringExpense expense = createMonthlyExpense();

            Supplier supplier = mock(Supplier.class);
            Category category = mock(Category.class);

            expense.update(
                    "Updated expense",
                    amount,
                    PaymentType.PIX,
                    supplier,
                    category,
                    null
            );

            assertEquals(supplier, expense.getSupplier());
            assertEquals(category, expense.getCategory());
        }

        @Test
        void shouldUpdateRecurringExpenseWithCreditCard() {
            RecurringExpense expense = createMonthlyExpense();
            CreditCard creditCard = mock(CreditCard.class);

            expense.update(
                    "Updated expense",
                    amount,
                    PaymentType.CREDIT,
                    null,
                    null,
                    creditCard
            );

            assertEquals(PaymentType.CREDIT, expense.getPaymentType());
            assertEquals(creditCard, expense.getCreditCard());

            verify(creditCard).hasLimit(amount);
        }

        @Test
        void shouldRemoveCreditCardWhenPaymentTypeChangesToPix() {
            CreditCard creditCard = mock(CreditCard.class);

            RecurringExpense expense = createRecurringExpense(
                    PaymentType.CREDIT,
                    RecurrenceType.MONTHLY,
                    1,
                    10,
                    startDate,
                    null,
                    creditCard
            );

            expense.update(
                    "Updated expense",
                    amount,
                    PaymentType.PIX,
                    null,
                    null,
                    null
            );

            assertEquals(PaymentType.PIX, expense.getPaymentType());
            assertNull(expense.getCreditCard());
        }

        @Test
        void shouldThrowWhenUpdatedAmountIsNull() {
            RecurringExpense expense = createMonthlyExpense();

            assertThrows(
                    InvalidAmountException.class,
                    () -> expense.update(
                            "Updated expense",
                            null,
                            PaymentType.PIX,
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenUpdatedDescriptionIsNull() {
            RecurringExpense expense = createMonthlyExpense();

            assertThrows(
                    DescriptionRequiredException.class,
                    () -> expense.update(
                            null,
                            amount,
                            PaymentType.PIX,
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenUpdatedDescriptionIsBlank() {
            RecurringExpense expense = createMonthlyExpense();

            assertThrows(
                    DescriptionRequiredException.class,
                    () -> expense.update(
                            " ",
                            amount,
                            PaymentType.PIX,
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenUpdatedPaymentTypeIsNull() {
            RecurringExpense expense = createMonthlyExpense();

            assertThrows(
                    PaymentTypeRequiredException.class,
                    () -> expense.update(
                            "Updated expense",
                            amount,
                            null,
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenUpdatedPaymentTypeRequiresCreditCard() {
            RecurringExpense expense = createMonthlyExpense();

            assertThrows(
                    CreditCardRequiredException.class,
                    () -> expense.update(
                            "Updated expense",
                            amount,
                            PaymentType.CREDIT,
                            null,
                            null,
                            null
                    )
            );
        }

        @Test
        void shouldThrowWhenUpdatedCreditCardLimitIsExceeded() {
            RecurringExpense expense = createMonthlyExpense();
            CreditCard creditCard = mock(CreditCard.class);

            doThrow(new CreditLimitExceededException("Credit limit exceeded"))
                    .when(creditCard)
                    .hasLimit(amount);

            assertThrows(
                    CreditLimitExceededException.class,
                    () -> expense.update(
                            "Updated expense",
                            amount,
                            PaymentType.CREDIT,
                            null,
                            null,
                            creditCard
                    )
            );
        }

        @Test
        void shouldNotChangeFieldsWhenUpdateValidationFails() {
            RecurringExpense expense = createMonthlyExpense();

            String originalDescription = expense.getDescription();
            Money originalAmount = expense.getAmount();
            PaymentType originalPaymentType = expense.getPaymentType();

            assertThrows(
                    CreditCardRequiredException.class,
                    () -> expense.update(
                            "New description",
                            Money.of(new BigDecimal("300.00")),
                            PaymentType.CREDIT,
                            null,
                            null,
                            null
                    )
            );

            assertEquals(originalDescription, expense.getDescription());
            assertEquals(originalAmount, expense.getAmount());
            assertEquals(originalPaymentType, expense.getPaymentType());
        }

        @Test
        void shouldPreserveRecurrenceDataWhenUpdating() {
            RecurringExpense expense = createMonthlyExpense();

            LocalDate originalStartDate = expense.getStartDate();
            LocalDate originalNextOccurrence = expense.getNextOccurrence();
            RecurrenceType originalRecurrenceType = expense.getRecurrenceType();
            Integer originalInterval = expense.getRecurrenceInterval();
            Integer originalDay = expense.getRecurrenceDay();

            expense.update(
                    "Updated expense",
                    amount,
                    PaymentType.PIX,
                    null,
                    null,
                    null
            );

            assertEquals(originalStartDate, expense.getStartDate());
            assertEquals(originalNextOccurrence, expense.getNextOccurrence());
            assertEquals(originalRecurrenceType, expense.getRecurrenceType());
            assertEquals(originalInterval, expense.getRecurrenceInterval());
            assertEquals(originalDay, expense.getRecurrenceDay());
        }
    }

    @Nested
    class CalculateFirstOccurrenceTests {

        @Test
        void shouldReturnStartDateForDailyRecurrence() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.DAILY,
                    1,
                    null,
                    startDate,
                    null,
                    null
            );

            assertEquals(startDate, expense.getNextOccurrence());
        }

        @Test
        void shouldReturnStartDateForWeeklyRecurrence() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.WEEKLY,
                    1,
                    null,
                    startDate,
                    null,
                    null
            );

            assertEquals(startDate, expense.getNextOccurrence());
        }

        @Test
        void shouldReturnStartDateForYearlyRecurrence() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.YEARLY,
                    1,
                    null,
                    startDate,
                    null,
                    null
            );

            assertEquals(startDate, expense.getNextOccurrence());
        }

        @Test
        void shouldUseRecurrenceDayInSameMonthWhenItIsAfterStartDate() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.MONTHLY,
                    1,
                    20,
                    LocalDate.of(2026, 1, 10),
                    null,
                    null
            );

            assertEquals(
                    LocalDate.of(2026, 1, 20),
                    expense.getNextOccurrence()
            );
        }

        @Test
        void shouldMoveToNextMonthWhenRecurrenceDayIsBeforeStartDate() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.MONTHLY,
                    1,
                    5,
                    LocalDate.of(2026, 1, 10),
                    null,
                    null
            );

            assertEquals(
                    LocalDate.of(2026, 2, 5),
                    expense.getNextOccurrence()
            );
        }

        @Test
        void shouldRespectRecurrenceIntervalWhenMovingToNextMonth() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.MONTHLY,
                    2,
                    5,
                    LocalDate.of(2026, 1, 10),
                    null,
                    null
            );

            assertEquals(
                    LocalDate.of(2026, 3, 5),
                    expense.getNextOccurrence()
            );
        }

        @Test
        void shouldAdjustRecurrenceDayToLastDayOfMonth() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.MONTHLY,
                    1,
                    31,
                    LocalDate.of(2026, 2, 1),
                    null,
                    null
            );

            assertEquals(
                    LocalDate.of(2026, 2, 28),
                    expense.getNextOccurrence()
            );
        }
    }

    @Nested
    class CalculateNextOccurrenceTests {

        @Test
        void shouldCalculateNextDailyOccurrence() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.DAILY,
                    2,
                    null,
                    startDate,
                    null,
                    null
            );

            assertEquals(
                    startDate.plusDays(2),
                    expense.calculateNextOccurrence()
            );
        }

        @Test
        void shouldCalculateNextWeeklyOccurrence() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.WEEKLY,
                    2,
                    null,
                    startDate,
                    null,
                    null
            );

            assertEquals(
                    startDate.plusWeeks(2),
                    expense.calculateNextOccurrence()
            );
        }

        @Test
        void shouldCalculateNextMonthlyOccurrence() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.MONTHLY,
                    2,
                    10,
                    startDate,
                    null,
                    null
            );

            assertEquals(
                    LocalDate.of(2026, 3, 10),
                    expense.calculateNextOccurrence()
            );
        }

        @Test
        void shouldAdjustNextMonthlyOccurrenceToLastDayOfMonth() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.MONTHLY,
                    1,
                    31,
                    LocalDate.of(2026, 1, 31),
                    null,
                    null
            );

            assertEquals(
                    LocalDate.of(2026, 2, 28),
                    expense.calculateNextOccurrence()
            );
        }

        @Test
        void shouldCalculateNextYearlyOccurrence() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.YEARLY,
                    2,
                    null,
                    startDate,
                    null,
                    null
            );

            assertEquals(
                    startDate.plusYears(2),
                    expense.calculateNextOccurrence()
            );
        }
    }

    @Nested
    class AdvanceNextOccurrenceTests {

        @Test
        void shouldAdvanceDailyOccurrence() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.DAILY,
                    1,
                    null,
                    startDate,
                    null,
                    null
            );

            expense.advanceNextOccurrence();

            assertEquals(
                    startDate.plusDays(1),
                    expense.getNextOccurrence()
            );
        }

        @Test
        void shouldAdvanceWeeklyOccurrence() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.WEEKLY,
                    1,
                    null,
                    startDate,
                    null,
                    null
            );

            expense.advanceNextOccurrence();

            assertEquals(
                    startDate.plusWeeks(1),
                    expense.getNextOccurrence()
            );
        }

        @Test
        void shouldAdvanceMonthlyOccurrence() {
            RecurringExpense expense = createMonthlyExpense();

            expense.advanceNextOccurrence();

            assertEquals(
                    LocalDate.of(2026, 2, 10),
                    expense.getNextOccurrence()
            );
        }

        @Test
        void shouldAdvanceYearlyOccurrence() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.YEARLY,
                    1,
                    null,
                    startDate,
                    null,
                    null
            );

            expense.advanceNextOccurrence();

            assertEquals(
                    startDate.plusYears(1),
                    expense.getNextOccurrence()
            );
        }

        @Test
        void shouldDeactivateWhenNextOccurrenceIsAfterEndDate() {
            LocalDate endDate = startDate;

            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.DAILY,
                    1,
                    null,
                    startDate,
                    endDate,
                    null
            );

            expense.advanceNextOccurrence();

            assertFalse(expense.isActive());

            // A próxima ocorrência não é alterada quando a recorrência é desativada.
            assertEquals(startDate, expense.getNextOccurrence());
        }

        @Test
        void shouldKeepActiveWhenNextOccurrenceIsEqualToEndDate() {
            LocalDate endDate = startDate.plusDays(1);

            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.DAILY,
                    1,
                    null,
                    startDate,
                    endDate,
                    null
            );

            expense.advanceNextOccurrence();

            assertTrue(expense.isActive());
            assertEquals(endDate, expense.getNextOccurrence());
        }
    }

    @Nested
    class IsDueTests {

        @Test
        void shouldReturnTrueWhenOccurrenceIsToday() {
            RecurringExpense expense = createMonthlyExpense();

            assertTrue(expense.isDue(startDate));
        }

        @Test
        void shouldReturnTrueWhenOccurrenceIsInThePast() {
            RecurringExpense expense = createMonthlyExpense();

            assertTrue(expense.isDue(startDate.plusDays(5)));
        }

        @Test
        void shouldReturnFalseWhenOccurrenceIsInTheFuture() {
            RecurringExpense expense = createMonthlyExpense();

            assertFalse(expense.isDue(startDate.minusDays(1)));
        }

        @Test
        void shouldReturnFalseWhenExpenseIsInactive() {
            RecurringExpense expense = createMonthlyExpense();

            expense.deactivate();

            assertFalse(expense.isDue(startDate.plusDays(5)));
        }

        @Test
        void shouldReturnFalseWhenOccurrenceIsAfterEndDate() {
            RecurringExpense expense = createRecurringExpense(
                    PaymentType.PIX,
                    RecurrenceType.DAILY,
                    1,
                    null,
                    startDate,
                    startDate.plusDays(1),
                    null
            );

            assertFalse(expense.isDue(startDate.minusDays(5)));
        }
    }

    @Nested
    class DeactivateTests {

        @Test
        void shouldDeactivateRecurringExpense() {
            RecurringExpense expense = createMonthlyExpense();

            expense.deactivate();

            assertFalse(expense.isActive());
        }

        @Test
        void shouldRemainInactiveWhenDeactivatedMoreThanOnce() {
            RecurringExpense expense = createMonthlyExpense();

            expense.deactivate();
            expense.deactivate();

            assertFalse(expense.isActive());
        }
    }
}

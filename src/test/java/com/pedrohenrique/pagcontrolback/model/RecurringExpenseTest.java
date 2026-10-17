
package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecurringExpenseTest {

    private User user;
    private CreditCard creditCard;
    private Money amount;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        creditCard = mock(CreditCard.class);
        amount = new Money(new BigDecimal("100.00"));
    }

    private RecurringExpense createMonthlyExpense(
            LocalDate startDate,
            Integer recurrenceDay,
            LocalDate recurrenceEndDate
    ) {
        return new RecurringExpense(
                "Internet",
                amount,
                PaymentType.PIX,
                RecurrenceType.MONTHLY,
                1,
                recurrenceDay,
                startDate,
                recurrenceEndDate,
                user,
                null,
                null,
                null
        );
    }

    @Nested
    @DisplayName("Creation")
    class CreationTests {

        @Test
        @DisplayName("Should create a recurring expense successfully")
        void shouldCreateRecurringExpense() {
            RecurringExpense expense = createMonthlyExpense(
                    LocalDate.of(2026, 1, 1),
                    10,
                    null
            );

            assertAll(
                    () -> assertEquals("Internet", expense.getDescription()),
                    () -> assertEquals(amount, expense.getAmount()),
                    () -> assertEquals(PaymentType.PIX, expense.getPaymentType()),
                    () -> assertEquals(RecurrenceType.MONTHLY,
                            expense.getRecurrenceType()),
                    () -> assertEquals(1, expense.getRecurrenceInterval()),
                    () -> assertEquals(LocalDate.of(2026, 1, 10),
                            expense.getNextOccurrence()),
                    () -> assertTrue(expense.isActive()),
                    () -> assertSame(user, expense.getUser())
            );
        }

        @Test
        @DisplayName("Should throw exception when description is null")
        void shouldThrowWhenDescriptionIsNull() {
            assertThrows(
                    DescriptionRequiredException.class,
                    () -> new RecurringExpense(
                            null, amount, PaymentType.PIX,
                            RecurrenceType.MONTHLY, 1, 10,
                            LocalDate.of(2026, 1, 1), null,
                            user, null, null, null
                    )
            );
        }

        @Test
        @DisplayName("Should throw exception when description is blank")
        void shouldThrowWhenDescriptionIsBlank() {
            assertThrows(
                    DescriptionRequiredException.class,
                    () -> new RecurringExpense(
                            "  ", amount, PaymentType.PIX,
                            RecurrenceType.MONTHLY, 1, 10,
                            LocalDate.of(2026, 1, 1), null,
                            user, null, null, null
                    )
            );
        }

        @Test
        @DisplayName("Should throw exception when payment type is null")
        void shouldThrowWhenPaymentTypeIsNull() {
            assertThrows(
                    PaymentTypeRequiredException.class,
                    () -> new RecurringExpense(
                            "Internet", amount, null,
                            RecurrenceType.MONTHLY, 1, 10,
                            LocalDate.of(2026, 1, 1), null,
                            user, null, null, null
                    )
            );
        }

        @Test
        @DisplayName("Should throw exception when recurrence type is null")
        void shouldThrowWhenRecurrenceTypeIsNull() {
            assertThrows(
                    RecurrenceTypeRequiredException.class,
                    () -> new RecurringExpense(
                            "Internet", amount, PaymentType.PIX,
                            null, 1, null,
                            LocalDate.of(2026, 1, 1), null,
                            user, null, null, null
                    )
            );
        }

        @Test
        @DisplayName("Should throw exception when recurrence interval is null")
        void shouldThrowWhenIntervalIsNull() {
            assertThrows(
                    RecurrenceIntervalException.class,
                    () -> new RecurringExpense(
                            "Internet", amount, PaymentType.PIX,
                            RecurrenceType.MONTHLY, null, 10,
                            LocalDate.of(2026, 1, 1), null,
                            user, null, null, null
                    )
            );
        }

        @Test
        @DisplayName("Should throw exception when recurrence interval is zero")
        void shouldThrowWhenIntervalIsZero() {
            assertThrows(
                    RecurrenceIntervalException.class,
                    () -> new RecurringExpense(
                            "Internet", amount, PaymentType.PIX,
                            RecurrenceType.MONTHLY, 0, 10,
                            LocalDate.of(2026, 1, 1), null,
                            user, null, null, null
                    )
            );
        }

        @Test
        @DisplayName("Should throw exception when recurrence interval is negative")
        void shouldThrowWhenIntervalIsNegative() {
            assertThrows(
                    RecurrenceIntervalException.class,
                    () -> new RecurringExpense(
                            "Internet", amount, PaymentType.PIX,
                            RecurrenceType.MONTHLY, -1, 10,
                            LocalDate.of(2026, 1, 1), null,
                            user, null, null, null
                    )
            );
        }

        @Test
        @DisplayName("Should throw exception when start date is null")
        void shouldThrowWhenStartDateIsNull() {
            assertThrows(
                    StartDateRequiredException.class,
                    () -> new RecurringExpense(
                            "Internet", amount, PaymentType.PIX,
                            RecurrenceType.MONTHLY, 1, 10,
                            null, null, user, null, null, null
                    )
            );
        }

        @Test
        @DisplayName("Should reject monthly recurrence day below one")
        void shouldRejectMonthlyDayBelowOne() {
            assertThrows(
                    RecurrenceDayException.class,
                    () -> createMonthlyExpense(
                            LocalDate.of(2026, 1, 1), 0, null
                    )
            );
        }

        @Test
        @DisplayName("Should reject monthly recurrence day above 31")
        void shouldRejectMonthlyDayAbove31() {
            assertThrows(
                    RecurrenceDayException.class,
                    () -> createMonthlyExpense(
                            LocalDate.of(2026, 1, 1), 32, null
                    )
            );
        }

        @Test
        @DisplayName("Should reject end date before start date")
        void shouldRejectEndDateBeforeStartDate() {
            assertThrows(
                    InvalidRecurrenceEndDateException.class,
                    () -> createMonthlyExpense(
                            LocalDate.of(2026, 1, 10),
                            15,
                            LocalDate.of(2026, 1, 9)
                    )
            );
        }
    }

    @Nested
    @DisplayName("Credit card validation")
    class CreditCardTests {

        @Test
        @DisplayName("Should require a credit card for CREDIT payment")
        void shouldRequireCreditCardForCreditPayment() {
            assertThrows(
                    CreditCardRequiredException.class,
                    () -> new RecurringExpense(
                            "Internet", amount, PaymentType.CREDIT,
                            RecurrenceType.MONTHLY, 1, 10,
                            LocalDate.of(2026, 1, 1), null,
                            user, null, null, null
                    )
            );
        }

        @Test
        @DisplayName("Should validate the credit card limit")
        void shouldValidateCreditCardLimit() {
            RecurringExpense expense = new RecurringExpense(
                    "Internet", amount, PaymentType.CREDIT,
                    RecurrenceType.MONTHLY, 1, 10,
                    LocalDate.of(2026, 1, 1), null,
                    user, null, null, creditCard
            );

            assertSame(creditCard, expense.getCreditCard());
            verify(creditCard).hasLimit(amount);
        }

        @Test
        @DisplayName("Should not require a credit card for PIX")
        void shouldNotRequireCreditCardForPix() {
            RecurringExpense expense = createMonthlyExpense(
                    LocalDate.of(2026, 1, 1), 10, null
            );

            assertNull(expense.getCreditCard());
        }
    }

    @Nested
    @DisplayName("First occurrence calculation")
    class FirstOccurrenceTests {

        @Test
        @DisplayName("Should use the recurrence day when it is after the start date")
        void shouldUseRecurrenceDayAfterStartDate() {
            RecurringExpense expense = createMonthlyExpense(
                    LocalDate.of(2026, 1, 5), 20, null
            );

            assertEquals(
                    LocalDate.of(2026, 1, 20),
                    expense.getNextOccurrence()
            );
        }

        @Test
        @DisplayName("Should move to next month when recurrence day has passed")
        void shouldMoveToNextMonthWhenDayHasPassed() {
            RecurringExpense expense = createMonthlyExpense(
                    LocalDate.of(2026, 1, 15), 10, null
            );

            assertEquals(
                    LocalDate.of(2026, 2, 10),
                    expense.getNextOccurrence()
            );
        }

        @Test
        @DisplayName("Should use the last day of February when recurrence day is 31")
        void shouldAdjustRecurrenceDayToLastDayOfFebruary() {
            RecurringExpense expense = createMonthlyExpense(
                    LocalDate.of(2026, 2, 1), 31, null
            );

            assertEquals(
                    LocalDate.of(2026, 2, 28),
                    expense.getNextOccurrence()
            );
        }

        @Test
        @DisplayName("Should use start date for non-monthly recurrence")
        void shouldUseStartDateForNonMonthlyRecurrence() {
            RecurringExpense expense = new RecurringExpense(
                    "Internet", amount, PaymentType.PIX,
                    RecurrenceType.WEEKLY, 1, null,
                    LocalDate.of(2026, 1, 5), null,
                    user, null, null, null
            );

            assertEquals(
                    LocalDate.of(2026, 1, 5),
                    expense.getNextOccurrence()
            );
        }
    }

    @Nested
    @DisplayName("Next occurrence calculation")
    class NextOccurrenceTests {

        @Test
        @DisplayName("Should calculate next daily occurrence")
        void shouldCalculateNextDailyOccurrence() {
            RecurringExpense expense = new RecurringExpense(
                    "Internet", amount, PaymentType.PIX,
                    RecurrenceType.DAILY, 2, null,
                    LocalDate.of(2026, 1, 1), null,
                    user, null, null, null
            );

            assertEquals(
                    LocalDate.of(2026, 1, 3),
                    expense.calculateNextOccurrence()
            );
        }

        @Test
        @DisplayName("Should calculate next weekly occurrence")
        void shouldCalculateNextWeeklyOccurrence() {
            RecurringExpense expense = new RecurringExpense(
                    "Internet", amount, PaymentType.PIX,
                    RecurrenceType.WEEKLY, 2, null,
                    LocalDate.of(2026, 1, 5), null,
                    user, null, null, null
            );

            assertEquals(
                    LocalDate.of(2026, 1, 19),
                    expense.calculateNextOccurrence()
            );
        }

        @Test
        @DisplayName("Should calculate next monthly occurrence")
        void shouldCalculateNextMonthlyOccurrence() {
            RecurringExpense expense = createMonthlyExpense(
                    LocalDate.of(2026, 1, 10), 10, null
            );

            assertEquals(
                    LocalDate.of(2026, 2, 10),
                    expense.calculateNextOccurrence()
            );
        }

        @Test
        @DisplayName("Should adjust next monthly occurrence to the last day of February")
        void shouldAdjustNextMonthlyOccurrenceToFebruaryEnd() {
            RecurringExpense expense = createMonthlyExpense(
                    LocalDate.of(2026, 1, 31), 31, null
            );

            assertEquals(
                    LocalDate.of(2026, 2, 28),
                    expense.calculateNextOccurrence()
            );
        }

        @Test
        @DisplayName("Should calculate next yearly occurrence")
        void shouldCalculateNextYearlyOccurrence() {
            RecurringExpense expense = new RecurringExpense(
                    "Insurance", amount, PaymentType.PIX,
                    RecurrenceType.YEARLY, 2, null,
                    LocalDate.of(2026, 5, 10), null,
                    user, null, null, null
            );

            assertEquals(
                    LocalDate.of(2028, 5, 10),
                    expense.calculateNextOccurrence()
            );
        }
    }

    @Nested
    @DisplayName("Advancing recurrence")
    class AdvanceOccurrenceTests {

        @Test
        @DisplayName("Should advance to the next occurrence")
        void shouldAdvanceNextOccurrence() {
            RecurringExpense expense = createMonthlyExpense(
                    LocalDate.of(2026, 1, 10), 10, null
            );

            expense.advanceNextOccurrence();

            assertEquals(
                    LocalDate.of(2026, 2, 10),
                    expense.getNextOccurrence()
            );
            assertTrue(expense.isActive());
        }

        @Test
        @DisplayName("Should deactivate when next occurrence exceeds end date")
        void shouldDeactivateWhenNextOccurrenceExceedsEndDate() {
            RecurringExpense expense = createMonthlyExpense(
                    LocalDate.of(2026, 1, 10),
                    10,
                    LocalDate.of(2026, 1, 31)
            );

            expense.advanceNextOccurrence();

            assertFalse(expense.isActive());
            assertEquals(
                    LocalDate.of(2026, 1, 10),
                    expense.getNextOccurrence()
            );
        }

        @Test
        @DisplayName("Should keep active when next occurrence equals end date")
        void shouldRemainActiveWhenNextOccurrenceEqualsEndDate() {
            RecurringExpense expense = createMonthlyExpense(
                    LocalDate.of(2026, 1, 10),
                    10,
                    LocalDate.of(2026, 2, 10)
            );

            expense.advanceNextOccurrence();

            assertTrue(expense.isActive());
            assertEquals(
                    LocalDate.of(2026, 2, 10),
                    expense.getNextOccurrence()
            );
        }
    }

    @Nested
    @DisplayName("Due date verification")
    class DueTests {

        @Test
        @DisplayName("Should be due when occurrence is today")
        void shouldBeDueWhenOccurrenceIsToday() {
            RecurringExpense expense = createMonthlyExpense(
                    LocalDate.of(2026, 1, 10), 10, null
            );

            assertTrue(expense.isDue(LocalDate.of(2026, 1, 10)));
        }

        @Test
        @DisplayName("Should be due when occurrence is in the past")
        void shouldBeDueWhenOccurrenceIsInThePast() {
            RecurringExpense expense = createMonthlyExpense(
                    LocalDate.of(2026, 1, 10), 10, null
            );

            assertTrue(expense.isDue(LocalDate.of(2026, 1, 15)));
        }

        @Test
        @DisplayName("Should not be due when occurrence is in the future")
        void shouldNotBeDueWhenOccurrenceIsInTheFuture() {
            RecurringExpense expense = createMonthlyExpense(
                    LocalDate.of(2026, 1, 10), 10, null
            );

            assertFalse(expense.isDue(LocalDate.of(2026, 1, 9)));
        }

        @Test
        @DisplayName("Should not be due when expense is inactive")
        void shouldNotBeDueWhenExpenseIsInactive() {
            RecurringExpense expense = createMonthlyExpense(
                    LocalDate.of(2026, 1, 10), 10, null
            );

            expense.deactivate();

            assertFalse(expense.isDue(LocalDate.of(2026, 1, 15)));
        }

        @Test
        @DisplayName("Should not be due after recurrence end date")
        void shouldNotBeDueAfterRecurrenceEndDate() {
            RecurringExpense expense = createMonthlyExpense(
                    LocalDate.of(2026, 1, 10),
                    10,
                    LocalDate.of(2026, 1, 10)
            );

            expense.advanceNextOccurrence();

            assertFalse(expense.isDue(LocalDate.of(2026, 2, 10)));
        }
    }

    @Nested
    @DisplayName("Deactivation")
    class DeactivationTests {

        @Test
        @DisplayName("Should deactivate recurring expense")
        void shouldDeactivateRecurringExpense() {
            RecurringExpense expense = createMonthlyExpense(
                    LocalDate.of(2026, 1, 10), 10, null
            );

            expense.deactivate();

            assertFalse(expense.isActive());
        }
    }
}

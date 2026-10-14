package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import com.pedrohenrique.pagcontrolback.repositories.RecurringExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateRecurringExpenseUseCaseTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private RecurringExpenseRepository recurringExpenseRepository;

    @InjectMocks
    private GenerateRecurringExpenseUseCase useCase;

    @Test
    void shouldNotGenerateExpensesWhenThereAreNoActiveRecurringExpenses() {
        useCase.execute();

        verify(expenseRepository, never()).save(any(Expense.class));
        verify(recurringExpenseRepository)
                .findActiveRecurringExpensesDueOnOrBefore(any(LocalDate.class));
    }

    @Test
    void shouldGenerateExpenseForDueRecurringExpenseUsingNextOccurrenceDate() {
        LocalDate today = LocalDate.now();

        RecurringExpense recurringExpense = createRecurringExpense(
                today,
                today.getDayOfMonth(),
                null
        );

        when(recurringExpenseRepository
                .findActiveRecurringExpensesDueOnOrBefore(any(LocalDate.class)))
                .thenReturn(List.of(recurringExpense));

        when(expenseRepository
                .existsByRecurringExpenseIdAndOccurrenceDate(
                        any(),
                        any(LocalDate.class)
                ))
                .thenReturn(false);

        useCase.execute();

        ArgumentCaptor<Expense> expenseCaptor =
                ArgumentCaptor.forClass(Expense.class);

        verify(expenseRepository).save(expenseCaptor.capture());

        Expense generatedExpense = expenseCaptor.getValue();

        assertEquals(today, generatedExpense.getExpenseDate());

        verify(recurringExpenseRepository).save(recurringExpense);
    }

    @Test
    void shouldNotGenerateDuplicateExpense() {
        LocalDate today = LocalDate.now();

        RecurringExpense recurringExpense = createRecurringExpense(
                today,
                today.getDayOfMonth(),
                null
        );

        when(recurringExpenseRepository
                .findActiveRecurringExpensesDueOnOrBefore(any(LocalDate.class)))
                .thenReturn(List.of(recurringExpense));

        when(expenseRepository
                .existsByRecurringExpenseIdAndOccurrenceDate(
                        any(),
                        any(LocalDate.class)
                ))
                .thenReturn(true);

        useCase.execute();

        verify(expenseRepository, never()).save(any(Expense.class));
        verify(recurringExpenseRepository).save(recurringExpense);
    }

    @Test
    void shouldGenerateAllMissedOccurrences() {
        LocalDate today = LocalDate.now();

        LocalDate startDate = today
                .minusMonths(2)
                .withDayOfMonth(1);

        RecurringExpense recurringExpense = createRecurringExpense(
                startDate,
                1,
                null
        );

        when(recurringExpenseRepository
                .findActiveRecurringExpensesDueOnOrBefore(any(LocalDate.class)))
                .thenReturn(List.of(recurringExpense));

        when(expenseRepository
                .existsByRecurringExpenseIdAndOccurrenceDate(
                        any(),
                        any(LocalDate.class)
                ))
                .thenReturn(false);

        useCase.execute();

        ArgumentCaptor<Expense> expenseCaptor =
                ArgumentCaptor.forClass(Expense.class);

        verify(expenseRepository, atLeast(2))
                .save(expenseCaptor.capture());

        List<Expense> generatedExpenses = expenseCaptor.getAllValues();

        for (Expense expense : generatedExpenses) {
            assertEquals(1, expense.getExpenseDate().getDayOfMonth());
        }

        verify(recurringExpenseRepository).save(recurringExpense);
    }

    private RecurringExpense createRecurringExpense(
            LocalDate startDate,
            Integer recurrenceDay,
            LocalDate recurrenceEndDate
    ) {

        User user = new User();
        return new RecurringExpense(
                "Internet",
                Money.of(new BigDecimal("100.00")),
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
}
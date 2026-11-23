
package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.RecurringExpenseNotFoundException;
import com.pedrohenrique.pagcontrolback.model.RecurringExpense;
import com.pedrohenrique.pagcontrolback.repositories.RecurringExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DeactivateRecurringExpenseUseCaseTest {

    private RecurringExpenseRepository recurringExpenseRepository;
    private DeactivateRecurringExpenseUseCase deactivateRecurringExpenseUseCase;

    private UUID recurringExpenseId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        recurringExpenseRepository = mock(RecurringExpenseRepository.class);

        deactivateRecurringExpenseUseCase =
                new DeactivateRecurringExpenseUseCase(recurringExpenseRepository);

        recurringExpenseId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }



    @Test
    void shouldDeactivateRecurringExpenseSuccessfully() {
        RecurringExpense recurringExpense = mock(RecurringExpense.class);

        when(recurringExpenseRepository
                .findRecurringExpenseByIdAndUserId(recurringExpenseId, userId))
                .thenReturn(Optional.of(recurringExpense));

        when(recurringExpense.isActive()).thenReturn(true);

        deactivateRecurringExpenseUseCase
                .deactivateRecurringExpense(recurringExpenseId, userId);

        verify(recurringExpense).deactivate();
        verify(recurringExpenseRepository).save(recurringExpense);
    }

    @Test
    void shouldNotDeactivateRecurringExpenseWhenAlreadyInactive() {
        RecurringExpense recurringExpense = mock(RecurringExpense.class);

        when(recurringExpenseRepository
                .findRecurringExpenseByIdAndUserId(recurringExpenseId, userId))
                .thenReturn(Optional.of(recurringExpense));

        when(recurringExpense.isActive()).thenReturn(false);

        deactivateRecurringExpenseUseCase
                .deactivateRecurringExpense(recurringExpenseId, userId);

        verify(recurringExpense, never()).deactivate();
        verify(recurringExpenseRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenRecurringExpenseIdIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deactivateRecurringExpenseUseCase
                        .deactivateRecurringExpense(null, userId)
        );

        assertEquals(
                "recurring expense id cannot be null",
                exception.getMessage()
        );

        verifyNoInteractions(recurringExpenseRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deactivateRecurringExpenseUseCase
                        .deactivateRecurringExpense(recurringExpenseId, null)
        );

        assertEquals("user id cannot be null", exception.getMessage());

        verifyNoInteractions(recurringExpenseRepository);
    }

    @Test
    void shouldThrowExceptionWhenRecurringExpenseDoesNotExist() {
        when(recurringExpenseRepository
                .findRecurringExpenseByIdAndUserId(recurringExpenseId, userId))
                .thenReturn(Optional.empty());

        RecurringExpenseNotFoundException exception = assertThrows(
                RecurringExpenseNotFoundException.class,
                () -> deactivateRecurringExpenseUseCase
                        .deactivateRecurringExpense(recurringExpenseId, userId)
        );

        assertEquals("recurring expense not found", exception.getMessage());

        verify(recurringExpenseRepository)
                .findRecurringExpenseByIdAndUserId(recurringExpenseId, userId);

        verify(recurringExpenseRepository, never()).save(any());
    }
}


package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.RecurringExpenseNotFoundException;
import com.pedrohenrique.pagcontrolback.model.RecurringExpense;
import com.pedrohenrique.pagcontrolback.repositories.RecurringExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetRecurringExpenseByIdUseCaseTest {

    @Mock
    private RecurringExpenseRepository recurringExpenseRepository;

    @InjectMocks
    private GetRecurringExpenseByIdUseCase getRecurringExpenseByIdUseCase;

    @Test
    void shouldReturnRecurringExpenseWhenFound() {
        UUID recurringExpenseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        RecurringExpense recurringExpense = mock(RecurringExpense.class);

        when(recurringExpenseRepository.findRecurringExpenseByIdAndUserId(
                recurringExpenseId,
                userId
        )).thenReturn(Optional.of(recurringExpense));

        RecurringExpense result =
                getRecurringExpenseByIdUseCase.findRecurringExpenseById(
                        recurringExpenseId,
                        userId
                );

        assertNotNull(result);
        assertEquals(recurringExpense, result);

        verify(recurringExpenseRepository, times(1))
                .findRecurringExpenseByIdAndUserId(recurringExpenseId, userId);
    }

    @Test
    void shouldThrowExceptionWhenRecurringExpenseNotFound() {
        UUID recurringExpenseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(recurringExpenseRepository.findRecurringExpenseByIdAndUserId(
                recurringExpenseId,
                userId
        )).thenReturn(Optional.empty());

        RecurringExpenseNotFoundException exception = assertThrows(
                RecurringExpenseNotFoundException.class,
                () -> getRecurringExpenseByIdUseCase.findRecurringExpenseById(
                        recurringExpenseId,
                        userId
                )
        );

        assertEquals("recurring expense not found", exception.getMessage());

        verify(recurringExpenseRepository, times(1))
                .findRecurringExpenseByIdAndUserId(recurringExpenseId, userId);
    }

    @Test
    void shouldThrowExceptionWhenRecurringExpenseIdIsNull() {
        UUID userId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getRecurringExpenseByIdUseCase.findRecurringExpenseById(
                        null,
                        userId
                )
        );

        assertEquals(
                "recurring expense id cannot be null",
                exception.getMessage()
        );

        verifyNoInteractions(recurringExpenseRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        UUID recurringExpenseId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getRecurringExpenseByIdUseCase.findRecurringExpenseById(
                        recurringExpenseId,
                        null
                )
        );

        assertEquals("user id cannot be null", exception.getMessage());

        verifyNoInteractions(recurringExpenseRepository);
    }

    @Test
    void shouldNotSearchRepositoryWhenBothIdsAreNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> getRecurringExpenseByIdUseCase.findRecurringExpenseById(
                        null,
                        null
                )
        );

        verifyNoInteractions(recurringExpenseRepository);
    }
}

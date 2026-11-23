
package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.model.RecurringExpense;
import com.pedrohenrique.pagcontrolback.repositories.RecurringExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListRecurringExpensesUseCaseTest {

    @Mock
    private RecurringExpenseRepository recurringExpenseRepository;

    @InjectMocks
    private ListRecurringExpensesUseCase listRecurringExpensesUseCase;

    @Test
    void shouldReturnAllRecurringExpensesByUserId() {
        UUID userId = UUID.randomUUID();

        List<RecurringExpense> recurringExpenses = List.of(
                mock(RecurringExpense.class),
                mock(RecurringExpense.class)
        );

        when(recurringExpenseRepository.findAllRecurringExpensesByUserId(userId))
                .thenReturn(recurringExpenses);

        List<RecurringExpense> result =
                listRecurringExpensesUseCase.findRecurringExpensesByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(recurringExpenses, result);

        verify(recurringExpenseRepository, times(1))
                .findAllRecurringExpensesByUserId(userId);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoRecurringExpenses() {
        UUID userId = UUID.randomUUID();

        when(recurringExpenseRepository.findAllRecurringExpensesByUserId(userId))
                .thenReturn(List.of());

        List<RecurringExpense> result =
                listRecurringExpensesUseCase.findRecurringExpensesByUserId(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(recurringExpenseRepository, times(1))
                .findAllRecurringExpensesByUserId(userId);
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> listRecurringExpensesUseCase.findRecurringExpensesByUserId(null)
        );

        assertEquals("user id cannot be null", exception.getMessage());

        verifyNoInteractions(recurringExpenseRepository);
    }
}

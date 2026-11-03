package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.ExpenseNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
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
class GetExpenseByIdUseCaseTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private GetExpenseByIdUseCase getExpenseByIdUseCase;

    private UUID expenseId;
    private UUID authenticatedUserId;
    private Expense expense;

    @BeforeEach
    void setUp() {
        expenseId = UUID.randomUUID();
        authenticatedUserId = UUID.randomUUID();

        expense = mock(Expense.class);
    }

    @Test
    void shouldReturnExpenseWhenExpenseExistsAndBelongsToAuthenticatedUser() {

        when(expenseRepository.findByExpenseIdAndUserId(expenseId, authenticatedUserId))
                .thenReturn(Optional.of(expense));

        Expense result = getExpenseByIdUseCase.getExpenseById(
                expenseId,
                authenticatedUserId
        );

        assertNotNull(result);
        assertSame(expense, result);

        verify(expenseRepository, times(1))
                .findByExpenseIdAndUserId(expenseId, authenticatedUserId);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenExpenseIdIsNull() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getExpenseByIdUseCase.getExpenseById(
                        null,
                        authenticatedUserId
                )
        );

        assertEquals("Expense ID cannot be null", exception.getMessage());

        verifyNoInteractions(expenseRepository);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenAuthenticatedUserIdIsNull() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getExpenseByIdUseCase.getExpenseById(
                        expenseId,
                        null
                )
        );

        assertEquals("Authenticated user ID cannot be null", exception.getMessage());

        verifyNoInteractions(expenseRepository);
    }

    @Test
    void shouldThrowExpenseNotFoundExceptionWhenExpenseDoesNotExist() {

        when(expenseRepository.findByExpenseIdAndUserId(expenseId, authenticatedUserId))
                .thenReturn(Optional.empty());

        assertThrows(
                ExpenseNotFoundException.class,
                () -> getExpenseByIdUseCase.getExpenseById(
                        expenseId,
                        authenticatedUserId
                )
        );


        verify(expenseRepository, times(1))
                .findByExpenseIdAndUserId(expenseId, authenticatedUserId);
    }

    @Test
    void shouldThrowExpenseNotFoundExceptionWhenExpenseDoesNotBelongToAuthenticatedUser() {

        when(expenseRepository.findByExpenseIdAndUserId(expenseId, authenticatedUserId))
                .thenReturn(Optional.empty());

        assertThrows(
                ExpenseNotFoundException.class,
                () -> getExpenseByIdUseCase.getExpenseById(
                        expenseId,
                        authenticatedUserId
                )
        );

        verify(expenseRepository, times(1))
                .findByExpenseIdAndUserId(expenseId, authenticatedUserId);
    }
}
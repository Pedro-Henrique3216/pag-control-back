
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
class DeleteExpenseUseCaseTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private DeleteExpenseUseCase useCase;

    private UUID expenseId;
    private UUID userId;
    private Expense expense;

    @BeforeEach
    void setUp() {
        expenseId = UUID.randomUUID();
        userId = UUID.randomUUID();
        expense = mock(Expense.class);
    }

    @Test
    void shouldDeleteExpenseSuccessfully() {
        when(expenseRepository.findActiveExpenseByIdAndUserId(
                expenseId, userId
        )).thenReturn(Optional.of(expense));

        useCase.deleteExpense(expenseId, userId);

        verify(expenseRepository).findActiveExpenseByIdAndUserId(
                expenseId, userId
        );

        verify(expense).deactivate();

        verify(expenseRepository).save(expense);
    }

    @Test
    void shouldThrowExceptionWhenExpenseIdIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.deleteExpense(null, userId)
        );

        verifyNoInteractions(expenseRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.deleteExpense(expenseId, null)
        );

        verifyNoInteractions(expenseRepository);
    }

    @Test
    void shouldThrowExceptionWhenExpenseDoesNotExist() {
        when(expenseRepository.findActiveExpenseByIdAndUserId(
                expenseId, userId
        )).thenReturn(Optional.empty());

        assertThrows(
                ExpenseNotFoundException.class,
                () -> useCase.deleteExpense(expenseId, userId)
        );

        verify(expenseRepository).findActiveExpenseByIdAndUserId(
                expenseId, userId
        );

        verify(expenseRepository, never()).save(any());

        verifyNoInteractions(expense);
    }

    @Test
    void shouldNotDeleteExpenseWhenItBelongsToAnotherUser() {
        when(expenseRepository.findActiveExpenseByIdAndUserId(
                expenseId, userId
        )).thenReturn(Optional.empty());

        assertThrows(
                ExpenseNotFoundException.class,
                () -> useCase.deleteExpense(expenseId, userId)
        );

        verify(expense, never()).deactivate();
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void shouldNotDeleteExpenseWhenItIsAlreadyInactive() {
        when(expenseRepository.findActiveExpenseByIdAndUserId(
                expenseId, userId
        )).thenReturn(Optional.empty());

        assertThrows(
                ExpenseNotFoundException.class,
                () -> useCase.deleteExpense(expenseId, userId)
        );

        verify(expenseRepository, never()).save(any());
    }
}

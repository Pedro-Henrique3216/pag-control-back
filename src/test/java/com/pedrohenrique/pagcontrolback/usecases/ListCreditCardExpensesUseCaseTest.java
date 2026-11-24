package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListCreditCardExpensesUseCaseTest {

    private ExpenseRepository expenseRepository;
    private ListCreditCardExpensesUseCase listCreditCardExpensesUseCase;

    @BeforeEach
    void setUp() {
        expenseRepository = mock(ExpenseRepository.class);

        listCreditCardExpensesUseCase =
                new ListCreditCardExpensesUseCase(expenseRepository);
    }

    @Test
    void shouldListExpensesByCreditCardIdAndUserId() {
        UUID creditCardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        List<Expense> expenses = List.of(
                mock(Expense.class),
                mock(Expense.class)
        );

        when(expenseRepository.findExpensesByCreditCardIdAndUserId(
                creditCardId, userId
        )).thenReturn(expenses);

        List<Expense> result =
                listCreditCardExpensesUseCase.listExpenseByCreditCardId(
                        creditCardId, userId
                );

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expenses, result);

        verify(expenseRepository).findExpensesByCreditCardIdAndUserId(
                creditCardId, userId
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoExpensesAreFound() {
        UUID creditCardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(expenseRepository.findExpensesByCreditCardIdAndUserId(
                creditCardId, userId
        )).thenReturn(List.of());

        List<Expense> result =
                listCreditCardExpensesUseCase.listExpenseByCreditCardId(
                        creditCardId, userId
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(expenseRepository).findExpensesByCreditCardIdAndUserId(
                creditCardId, userId
        );
    }

    @Test
    void shouldThrowExceptionWhenCreditCardIdIsNull() {
        UUID userId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> listCreditCardExpensesUseCase.listExpenseByCreditCardId(
                        null, userId
                )
        );

        assertEquals("credit card id cannot be null", exception.getMessage());

        verifyNoInteractions(expenseRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        UUID creditCardId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> listCreditCardExpensesUseCase.listExpenseByCreditCardId(
                        creditCardId, null
                )
        );

        assertEquals("user id cannot be null", exception.getMessage());

        verifyNoInteractions(expenseRepository);
    }
}

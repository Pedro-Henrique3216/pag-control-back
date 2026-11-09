
package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.command.UpdateExpenseFinancialDataCommand;
import com.pedrohenrique.pagcontrolback.exceptions.ExpenseNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateExpenseFinancialDataUseCaseTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private UpdateExpenseFinancialDataUseCase useCase;

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
    void shouldUpdateFinancialDataSuccessfully() {
        LocalDate expenseDate = LocalDate.of(2026, 11, 10);
        BigDecimal amount = new BigDecimal("300.00");

        UpdateExpenseFinancialDataCommand command =
                new UpdateExpenseFinancialDataCommand(expenseDate, amount);

        when(expenseRepository.findActiveExpenseByIdAndUserId(expenseId, userId))
                .thenReturn(Optional.of(expense));

        when(expenseRepository.save(expense)).thenReturn(expense);

        Expense result = useCase.updateFinancialData(
                expenseId,
                userId,
                command
        );

        assertNotNull(result);
        assertSame(expense, result);

        verify(expenseRepository).findActiveExpenseByIdAndUserId(expenseId, userId);

        verify(expense).updateFinancialData(
                expenseDate,
                Money.of(amount)
        );

        verify(expenseRepository).save(expense);
    }

    @Test
    void shouldUpdateOnlyExpenseDateWhenTotalAmountIsNull() {
        LocalDate expenseDate = LocalDate.of(2026, 11, 15);

        UpdateExpenseFinancialDataCommand command =
                new UpdateExpenseFinancialDataCommand(expenseDate, null);

        when(expenseRepository.findActiveExpenseByIdAndUserId(expenseId, userId))
                .thenReturn(Optional.of(expense));

        when(expenseRepository.save(expense)).thenReturn(expense);

        useCase.updateFinancialData(expenseId, userId, command);

        verify(expense).updateFinancialData(expenseDate, null);

        verify(expenseRepository).save(expense);
    }

    @Test
    void shouldUpdateOnlyTotalAmountWhenExpenseDateIsNull() {
        BigDecimal amount = new BigDecimal("450.00");

        UpdateExpenseFinancialDataCommand command =
                new UpdateExpenseFinancialDataCommand(null, amount);

        when(expenseRepository.findActiveExpenseByIdAndUserId(expenseId, userId))
                .thenReturn(Optional.of(expense));

        when(expenseRepository.save(expense)).thenReturn(expense);

        useCase.updateFinancialData(expenseId, userId, command);

        verify(expense).updateFinancialData(
                null,
                Money.of(amount)
        );

        verify(expenseRepository).save(expense);
    }

    @Test
    void shouldThrowExceptionWhenExpenseIdIsNull() {
        UpdateExpenseFinancialDataCommand command =
                new UpdateExpenseFinancialDataCommand(
                        LocalDate.now(),
                        new BigDecimal("100.00")
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.updateFinancialData(null, userId, command)
        );

        verifyNoInteractions(expenseRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        UpdateExpenseFinancialDataCommand command =
                new UpdateExpenseFinancialDataCommand(
                        LocalDate.now(),
                        new BigDecimal("100.00")
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.updateFinancialData(expenseId, null, command)
        );

        verifyNoInteractions(expenseRepository);
    }

    @Test
    void shouldThrowExceptionWhenCommandIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.updateFinancialData(expenseId, userId, null)
        );

        verifyNoInteractions(expenseRepository);
    }

    @Test
    void shouldThrowExceptionWhenExpenseDoesNotExist() {
        UpdateExpenseFinancialDataCommand command =
                new UpdateExpenseFinancialDataCommand(
                        LocalDate.of(2026, 11, 10),
                        new BigDecimal("200.00")
                );

        when(expenseRepository.findActiveExpenseByIdAndUserId(expenseId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ExpenseNotFoundException.class,
                () -> useCase.updateFinancialData(
                        expenseId,
                        userId,
                        command
                )
        );

        verify(expenseRepository, never()).save(any());
    }
}

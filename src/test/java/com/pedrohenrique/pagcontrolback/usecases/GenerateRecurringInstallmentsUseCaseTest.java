package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateRecurringInstallmentsUseCaseTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private GenerateRecurringInstallmentsUseCase useCase;

    @Nested
    class ExecuteTests {

        @Test
        void shouldGenerateInstallmentsForAllRecurringExpenses() {

            Expense expense1 = mock(Expense.class);
            Expense expense2 = mock(Expense.class);

            when(expenseRepository.findAllActiveRecurringExpenses())
                    .thenReturn(List.of(expense1, expense2));

            useCase.execute();

            verify(expense1).generateNextInstallment();
            verify(expense2).generateNextInstallment();
        }

        @Test
        void shouldNotGenerateInstallmentsWhenNoRecurringExpensesExist() {

            when(expenseRepository.findAllActiveRecurringExpenses())
                    .thenReturn(List.of());

            useCase.execute();

            verify(expenseRepository)
                    .findAllActiveRecurringExpenses();

            verifyNoMoreInteractions(expenseRepository);
        }

        @Test
        void shouldGenerateInstallmentOnlyOncePerExpense() {

            Expense expense = mock(Expense.class);

            when(expenseRepository.findAllActiveRecurringExpenses())
                    .thenReturn(List.of(expense));

            useCase.execute();

            verify(expense, times(1))
                    .generateNextInstallment();
        }
    }
}
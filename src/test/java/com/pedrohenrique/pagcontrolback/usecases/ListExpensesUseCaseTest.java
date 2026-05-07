package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.request.ListExpensesQuery;
import com.pedrohenrique.pagcontrolback.exceptions.FutureMonthNotAllowedException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.UserIdRequiredException;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.model.PaymentType;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepositoryCustom;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListExpensesUseCaseTest {

    @Mock
    private ExpenseRepositoryCustom expenseRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private ListExpensesUseCase listExpensesUseCase;

    @Test
    void shouldThrowUserIdRequiredException_whenUserIdIsNull() {
        var query = new ListExpensesQuery(
                null,
                null,
                null
        );

        var exception = assertThrows(
                UserIdRequiredException.class,
                () -> listExpensesUseCase.execute(query, null)
        );

        assertEquals("User id is required.", exception.getMessage());
    }

    @Test
    void shouldThrowSupplierNotFoundException_whenSupplierIdInQueryDoesNotExist(){
        UUID userId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();

        var query = new ListExpensesQuery(
                null,
                supplierId,
                null
        );

        when(supplierRepository.existsById(supplierId)).thenReturn(false);

        var exception = assertThrows(
                SupplierNotFoundException.class,
                () -> listExpensesUseCase.execute(query, userId)
        );

        assertEquals("Supplier not found.", exception.getMessage());
    }

    @Test
    void shouldReturnEmptyList_whenNoExpensesMatchQuery() {

        UUID userId = UUID.randomUUID();

        var query = new ListExpensesQuery(
                null,
                null,
                null
        );

        when(expenseRepository.search(query, userId))
                .thenReturn(List.of());

        var result = listExpensesUseCase.execute(query, userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(expenseRepository).search(query, userId);
    }

    @Test
    void shouldReturnExpenses_whenQueryIsValid() {

        UUID userId = UUID.randomUUID();

        var query = new ListExpensesQuery(
                null,
                null,
                null
        );


        User user = mock(User.class);
        Supplier supplier = new Supplier("Supplier test", null, user);

        Expense expense = new Expense(
                "INV-1",
                PaymentType.CREDIT,
                LocalDate.now(),
                user,
                supplier
        );

        when(expenseRepository.search(query, userId))
                .thenReturn(List.of(expense));

        var result = listExpensesUseCase.execute(query, userId);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(expenseRepository).search(query, userId);
    }

    @Test
    void shouldPassUserIdToRepositorySearch() {
        UUID userId = UUID.randomUUID();

        var query = new ListExpensesQuery(
                null,
                null,
                null
        );

        listExpensesUseCase.execute(query, userId);

        verify(expenseRepository).search(query, userId);
    }

    @Test
    void shouldThrowException_whenMonthIsInFuture() {

        UUID userId = UUID.randomUUID();

        var query = new ListExpensesQuery(
                YearMonth.now().plusMonths(1),
                null,
                null
        );

        assertThrows(
                FutureMonthNotAllowedException.class,
                () -> listExpensesUseCase.execute(query, userId)
        );

        verify(expenseRepository, never())
                .search(any(), any());
    }

}
package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.request.ListExpensesQuery;
import com.pedrohenrique.pagcontrolback.exceptions.FutureMonthNotAllowedException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.UserIdRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.model.PaymentType;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepositoryCustom;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
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
    private UserRepository userRepository;

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
    void shouldThrowUserNotFoundException_whenUserDoesNotExist(){
        UUID userId = UUID.randomUUID();

        var query = new ListExpensesQuery(
                null,
                null,
                null
        );

        when(userRepository.existsById(userId)).thenReturn(false);

        var exception = assertThrows(
                UserNotFoundException.class,
                () -> listExpensesUseCase.execute(query, userId)
        );

        assertEquals("User not found.", exception.getMessage());
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

        when(userRepository.existsById(userId)).thenReturn(true);
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

        when(userRepository.existsById(userId)).thenReturn(true);

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

        Supplier supplier = new Supplier("Supplier test");
        User user = mock(User.class);

        Expense expense = new Expense(
                "INV-1",
                PaymentType.CREDIT,
                LocalDate.now(),
                user,
                supplier
        );

        when(userRepository.existsById(userId)).thenReturn(true);

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

        when(userRepository.existsById(userId)).thenReturn(true);

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

        when(userRepository.existsById(userId)).thenReturn(true);

        assertThrows(
                FutureMonthNotAllowedException.class,
                () -> listExpensesUseCase.execute(query, userId)
        );

        verify(expenseRepository, never())
                .search(any(), any());
    }

}
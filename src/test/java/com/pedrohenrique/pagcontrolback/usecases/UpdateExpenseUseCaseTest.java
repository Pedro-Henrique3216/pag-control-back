
package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.UpdateExpenseCommand;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.ExpenseNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
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
class UpdateExpenseUseCaseTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private UpdateExpenseUseCase useCase;

    private UUID expenseId;
    private UUID userId;
    private UUID supplierId;
    private UUID categoryId;

    private Expense expense;
    private Supplier supplier;
    private Category category;

    @BeforeEach
    void setUp() {
        expenseId = UUID.randomUUID();
        userId = UUID.randomUUID();
        supplierId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        expense = mock(Expense.class);
        supplier = mock(Supplier.class);
        category = mock(Category.class);
    }

    @Test
    void shouldUpdateExpenseSuccessfully() {
        UpdateExpenseCommand command = new UpdateExpenseCommand(
                "Updated description",
                "INV-123",
                supplierId,
                categoryId
        );

        when(expenseRepository.findActiveExpenseByIdAndUserId(expenseId, userId))
                .thenReturn(Optional.of(expense));

        when(supplierRepository.findByIdAndUser_Id(supplierId, userId))
                .thenReturn(Optional.of(supplier));

        when(categoryRepository.findCategoryByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(category));

        when(expenseRepository.save(expense)).thenReturn(expense);

        Expense result = useCase.updateExpense(expenseId, userId, command);

        assertNotNull(result);
        assertSame(expense, result);

        verify(expense).updateDetails(
                "Updated description",
                "INV-123",
                supplier,
                category
        );

        verify(expenseRepository).save(expense);
    }

    @Test
    void shouldUpdateExpenseWithoutSupplierAndCategory() {
        UpdateExpenseCommand command = new UpdateExpenseCommand(
                "Updated description",
                "INV-456",
                null,
                null
        );

        when(expenseRepository.findActiveExpenseByIdAndUserId(expenseId, userId))
                .thenReturn(Optional.of(expense));

        when(expenseRepository.save(expense)).thenReturn(expense);

        Expense result = useCase.updateExpense(expenseId, userId, command);

        assertNotNull(result);

        verify(expense).updateDetails(
                "Updated description",
                "INV-456",
                null,
                null
        );

        verifyNoInteractions(supplierRepository, categoryRepository);
        verify(expenseRepository).save(expense);
    }

    @Test
    void shouldThrowExceptionWhenExpenseIdIsNull() {
        UpdateExpenseCommand command = createCommand();

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.updateExpense(null, userId, command)
        );

        verifyNoInteractions(
                expenseRepository,
                supplierRepository,
                categoryRepository
        );
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        UpdateExpenseCommand command = createCommand();

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.updateExpense(expenseId, null, command)
        );

        verifyNoInteractions(
                expenseRepository,
                supplierRepository,
                categoryRepository
        );
    }

    @Test
    void shouldThrowExceptionWhenCommandIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.updateExpense(expenseId, userId, null)
        );

        verifyNoInteractions(
                expenseRepository,
                supplierRepository,
                categoryRepository
        );
    }

    @Test
    void shouldThrowExceptionWhenExpenseDoesNotExist() {
        UpdateExpenseCommand command = createCommand();

        when(expenseRepository.findActiveExpenseByIdAndUserId(expenseId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ExpenseNotFoundException.class,
                () -> useCase.updateExpense(expenseId, userId, command)
        );

        verify(expenseRepository, never()).save(any());
        verifyNoInteractions(supplierRepository, categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenSupplierDoesNotExist() {
        UpdateExpenseCommand command = new UpdateExpenseCommand(
                "Updated description",
                "INV-123",
                supplierId,
                null
        );

        when(expenseRepository.findActiveExpenseByIdAndUserId(expenseId, userId))
                .thenReturn(Optional.of(expense));

        when(supplierRepository.findByIdAndUser_Id(supplierId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                SupplierNotFoundException.class,
                () -> useCase.updateExpense(expenseId, userId, command)
        );

        verify(expense, never()).updateDetails(any(), any(), any(), any());
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        UpdateExpenseCommand command = new UpdateExpenseCommand(
                "Updated description",
                "INV-123",
                null,
                categoryId
        );

        when(expenseRepository.findActiveExpenseByIdAndUserId(expenseId, userId))
                .thenReturn(Optional.of(expense));

        when(categoryRepository.findCategoryByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> useCase.updateExpense(expenseId, userId, command)
        );

        verify(expense, never()).updateDetails(any(), any(), any(), any());
        verify(expenseRepository, never()).save(any());
    }

    private UpdateExpenseCommand createCommand() {
        return new UpdateExpenseCommand(
                "Updated description",
                "INV-123",
                null,
                null
        );
    }
}

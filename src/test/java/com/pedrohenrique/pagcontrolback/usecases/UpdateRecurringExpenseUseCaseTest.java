package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.command.UpdateRecurringExpenseCommand;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.RecurringExpenseNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierNotFoundException;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.CreditCardRepository;
import com.pedrohenrique.pagcontrolback.repositories.RecurringExpenseRepository;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateRecurringExpenseUseCaseTest {

    @Mock
    private RecurringExpenseRepository recurringExpenseRepository;

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RecurringExpense recurringExpense;

    @Mock
    private Supplier supplier;

    @Mock
    private Category category;

    @Mock
    private CreditCard creditCard;

    @InjectMocks
    private UpdateRecurringExpenseUseCase updateRecurringExpenseUseCase;

    private final UUID recurringExpenseId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID supplierId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();
    private final UUID creditCardId = UUID.randomUUID();

    private UpdateRecurringExpenseCommand createCommand(
            PaymentType paymentType,
            UUID supplierId,
            UUID categoryId,
            UUID creditCardId
    ) {
        return new UpdateRecurringExpenseCommand(
                "Updated expense",
                new BigDecimal("250.00"),
                paymentType,
                supplierId,
                categoryId,
                creditCardId
        );
    }

    @Test
    void shouldUpdateRecurringExpenseSuccessfully() {
        UpdateRecurringExpenseCommand command = createCommand(
                PaymentType.PIX, null, null, null
        );

        when(recurringExpenseRepository
                .findRecurringExpenseByIdAndUserId(recurringExpenseId, userId))
                .thenReturn(Optional.of(recurringExpense));

        when(recurringExpenseRepository.save(recurringExpense))
                .thenReturn(recurringExpense);

        RecurringExpense result =
                updateRecurringExpenseUseCase.updateRecurringExpenseById(
                        recurringExpenseId, userId, command
                );

        assertNotNull(result);
        assertEquals(recurringExpense, result);

        verify(recurringExpense).update(
                command.description(),
                Money.of(command.amount()),
                command.paymentType(),
                null,
                null,
                null
        );

        verify(recurringExpenseRepository).save(recurringExpense);
        verifyNoInteractions(creditCardRepository, supplierRepository, categoryRepository);
    }

    @Test
    void shouldUpdateRecurringExpenseWithSupplierAndCategory() {
        UpdateRecurringExpenseCommand command = createCommand(
                PaymentType.PIX, supplierId, categoryId, null
        );

        when(recurringExpenseRepository
                .findRecurringExpenseByIdAndUserId(recurringExpenseId, userId))
                .thenReturn(Optional.of(recurringExpense));

        when(supplierRepository.findByIdAndUser_Id(supplierId, userId))
                .thenReturn(Optional.of(supplier));

        when(categoryRepository.findCategoryByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(category));

        when(recurringExpenseRepository.save(recurringExpense))
                .thenReturn(recurringExpense);

        RecurringExpense result =
                updateRecurringExpenseUseCase.updateRecurringExpenseById(
                        recurringExpenseId, userId, command
                );

        assertNotNull(result);

        verify(recurringExpense).update(
                command.description(),
                Money.of(command.amount()),
                command.paymentType(),
                supplier,
                category,
                null
        );

        verify(supplierRepository).findByIdAndUser_Id(supplierId, userId);
        verify(categoryRepository).findCategoryByIdAndUserId(categoryId, userId);
        verify(recurringExpenseRepository).save(recurringExpense);
    }

    @Test
    void shouldUpdateRecurringExpenseWithCreditCard() {
        UpdateRecurringExpenseCommand command = createCommand(
                PaymentType.CREDIT, null, null, creditCardId
        );

        when(recurringExpenseRepository
                .findRecurringExpenseByIdAndUserId(recurringExpenseId, userId))
                .thenReturn(Optional.of(recurringExpense));

        when(creditCardRepository
                .findCreditCardByCardIdAndUserId(creditCardId, userId))
                .thenReturn(creditCard);

        when(recurringExpenseRepository.save(recurringExpense))
                .thenReturn(recurringExpense);

        RecurringExpense result =
                updateRecurringExpenseUseCase.updateRecurringExpenseById(
                        recurringExpenseId, userId, command
                );

        assertNotNull(result);

        verify(recurringExpense).update(
                command.description(),
                Money.of(command.amount()),
                command.paymentType(),
                null,
                null,
                creditCard
        );

        verify(creditCardRepository)
                .findCreditCardByCardIdAndUserId(creditCardId, userId);

        verify(recurringExpenseRepository).save(recurringExpense);
    }

    @Test
    void shouldThrowExceptionWhenRecurringExpenseDoesNotExist() {
        UpdateRecurringExpenseCommand command = createCommand(
                PaymentType.PIX, null, null, null
        );

        when(recurringExpenseRepository
                .findRecurringExpenseByIdAndUserId(recurringExpenseId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecurringExpenseNotFoundException.class,
                () -> updateRecurringExpenseUseCase.updateRecurringExpenseById(
                        recurringExpenseId, userId, command
                )
        );

        verify(recurringExpenseRepository, never()).save(any());
        verifyNoInteractions(creditCardRepository, supplierRepository, categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenSupplierDoesNotExist() {
        UpdateRecurringExpenseCommand command = createCommand(
                PaymentType.PIX, supplierId, null, null
        );

        when(recurringExpenseRepository
                .findRecurringExpenseByIdAndUserId(recurringExpenseId, userId))
                .thenReturn(Optional.of(recurringExpense));

        when(supplierRepository.findByIdAndUser_Id(supplierId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                SupplierNotFoundException.class,
                () -> updateRecurringExpenseUseCase.updateRecurringExpenseById(
                        recurringExpenseId, userId, command
                )
        );

        verify(recurringExpenseRepository, never()).save(any());
        verifyNoInteractions(categoryRepository, creditCardRepository);
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        UpdateRecurringExpenseCommand command = createCommand(
                PaymentType.PIX, null, categoryId, null
        );

        when(recurringExpenseRepository
                .findRecurringExpenseByIdAndUserId(recurringExpenseId, userId))
                .thenReturn(Optional.of(recurringExpense));

        when(categoryRepository.findCategoryByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> updateRecurringExpenseUseCase.updateRecurringExpenseById(
                        recurringExpenseId, userId, command
                )
        );

        verify(recurringExpenseRepository, never()).save(any());
        verifyNoInteractions(supplierRepository, creditCardRepository);
    }
}

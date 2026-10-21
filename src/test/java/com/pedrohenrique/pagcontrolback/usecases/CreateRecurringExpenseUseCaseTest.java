
package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.CreateRecurringExpenseCommand;
import com.pedrohenrique.pagcontrolback.exceptions.*;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class CreateRecurringExpenseUseCaseTest {

    @Mock
    private RecurringExpenseRepository recurringExpenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private User user;

    @Mock
    private CreditCard creditCard;

    @Mock
    private Supplier supplier;

    @Mock
    private Category category;

    @InjectMocks
    private CreateRecurringExpenseUseCase useCase;

    private UUID userId;
    private CreateRecurringExpenseCommand command;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        command = createCommand(
                PaymentType.PIX,
                null,
                null,
                null
        );
    }

    private CreateRecurringExpenseCommand createCommand(
            PaymentType paymentType,
            UUID supplierId,
            UUID creditCardId,
            UUID categoryId
    ) {
        return new CreateRecurringExpenseCommand(
                "Internet",
                paymentType,
                supplierId,
                LocalDate.of(2026, 1, 1),
                creditCardId,
                new BigDecimal("100.00"),
                categoryId,
                10,
                RecurrenceType.MONTHLY,
                1,
                null
        );
    }

    @Test
    void shouldCreateAndSaveRecurringExpense() {
        when(userRepository.getReferenceById(userId))
                .thenReturn(user);

        useCase.execute(command, userId);

        ArgumentCaptor<RecurringExpense> captor =
                ArgumentCaptor.forClass(RecurringExpense.class);

        verify(recurringExpenseRepository).save(captor.capture());

        RecurringExpense savedExpense = captor.getValue();

        assertNotNull(savedExpense);
        assertEquals("Internet", savedExpense.getDescription());
        assertEquals(PaymentType.PIX, savedExpense.getPaymentType());
        assertEquals(10, savedExpense.getRecurrenceDay());
        assertEquals(user, savedExpense.getUser());

        verify(userRepository).getReferenceById(userId);
        verifyNoInteractions(creditCardRepository);
    }

    @Test
    void shouldThrowExceptionWhenCommandIsNull() {
        assertThrows(
                CreateExpenseCommandRequiredException.class,
                () -> useCase.execute(null, userId)
        );

        verifyNoInteractions(recurringExpenseRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        assertThrows(
                UserIdRequiredException.class,
                () -> useCase.execute(command, null)
        );

        verifyNoInteractions(recurringExpenseRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldCreateRecurringExpenseWithSupplier() {
        UUID supplierId = UUID.randomUUID();

        command = createCommand(
                PaymentType.PIX,
                supplierId,
                null,
                null
        );

        when(userRepository.getReferenceById(userId))
                .thenReturn(user);

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));

        useCase.execute(command, userId);

        ArgumentCaptor<RecurringExpense> captor =
                ArgumentCaptor.forClass(RecurringExpense.class);

        verify(recurringExpenseRepository).save(captor.capture());

        assertEquals(supplier, captor.getValue().getSupplier());

        verify(supplierRepository).findById(supplierId);
    }

    @Test
    void shouldThrowExceptionWhenSupplierDoesNotExist() {
        UUID supplierId = UUID.randomUUID();

        command = createCommand(
                PaymentType.PIX,
                supplierId,
                null,
                null
        );

        when(userRepository.getReferenceById(userId))
                .thenReturn(user);

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.empty());

        assertThrows(
                SupplierNotFoundException.class,
                () -> useCase.execute(command, userId)
        );

        verify(recurringExpenseRepository, never())
                .save(any(RecurringExpense.class));
    }

    @Test
    void shouldCreateRecurringExpenseWithCategory() {
        UUID categoryId = UUID.randomUUID();

        command = createCommand(
                PaymentType.PIX,
                null,
                null,
                categoryId
        );

        when(userRepository.getReferenceById(userId))
                .thenReturn(user);

        when(categoryRepository.findCategoryByIdAndUserId(
                categoryId, userId
        )).thenReturn(Optional.of(category));

        useCase.execute(command, userId);

        ArgumentCaptor<RecurringExpense> captor =
                ArgumentCaptor.forClass(RecurringExpense.class);

        verify(recurringExpenseRepository).save(captor.capture());

        assertEquals(category, captor.getValue().getCategory());

        verify(categoryRepository).findCategoryByIdAndUserId(
                categoryId, userId
        );
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        UUID categoryId = UUID.randomUUID();

        command = createCommand(
                PaymentType.PIX,
                null,
                null,
                categoryId
        );

        when(userRepository.getReferenceById(userId))
                .thenReturn(user);

        when(categoryRepository.findCategoryByIdAndUserId(
                categoryId, userId
        )).thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> useCase.execute(command, userId)
        );

        verify(recurringExpenseRepository, never())
                .save(any(RecurringExpense.class));
    }

    @Test
    void shouldCreateRecurringExpenseWithCreditCard() {
        UUID creditCardId = UUID.randomUUID();

        command = createCommand(
                PaymentType.CREDIT,
                null,
                creditCardId,
                null
        );

        when(userRepository.getReferenceById(userId))
                .thenReturn(user);

        when(creditCardRepository.findCreditCardByCardIdAndUserId(
                creditCardId, userId
        )).thenReturn(creditCard);

        when(creditCard.isActive()).thenReturn(true);

        useCase.execute(command, userId);

        ArgumentCaptor<RecurringExpense> captor =
                ArgumentCaptor.forClass(RecurringExpense.class);

        verify(recurringExpenseRepository).save(captor.capture());

        assertEquals(creditCard, captor.getValue().getCreditCard());

        verify(creditCardRepository)
                .findCreditCardByCardIdAndUserId(creditCardId, userId);

        verify(creditCard).isActive();
    }

    @Test
    void shouldThrowExceptionWhenCreditCardIsRequired() {
        command = createCommand(
                PaymentType.CREDIT,
                null,
                null,
                null
        );

        when(userRepository.getReferenceById(userId))
                .thenReturn(user);

        assertThrows(
                CreditCardRequiredException.class,
                () -> useCase.execute(command, userId)
        );

        verifyNoInteractions(creditCardRepository);

        verify(recurringExpenseRepository, never())
                .save(any(RecurringExpense.class));
    }

    @Test
    void shouldThrowExceptionWhenCreditCardDoesNotExist() {
        UUID creditCardId = UUID.randomUUID();

        command = createCommand(
                PaymentType.CREDIT,
                null,
                creditCardId,
                null
        );

        when(userRepository.getReferenceById(userId))
                .thenReturn(user);

        when(creditCardRepository.findCreditCardByCardIdAndUserId(
                creditCardId, userId
        )).thenReturn(null);

        assertThrows(
                CreditCardNotFoundException.class,
                () -> useCase.execute(command, userId)
        );

        verify(recurringExpenseRepository, never())
                .save(any(RecurringExpense.class));
    }

    @Test
    void shouldThrowExceptionWhenCreditCardIsInactive() {
        UUID creditCardId = UUID.randomUUID();

        command = createCommand(
                PaymentType.CREDIT,
                null,
                creditCardId,
                null
        );

        when(userRepository.getReferenceById(userId))
                .thenReturn(user);

        when(creditCardRepository.findCreditCardByCardIdAndUserId(
                creditCardId, userId
        )).thenReturn(creditCard);

        when(creditCard.isActive()).thenReturn(false);

        assertThrows(
                CreditCardInactiveException.class,
                () -> useCase.execute(command, userId)
        );

        verify(creditCard).isActive();

        verify(recurringExpenseRepository, never())
                .save(any(RecurringExpense.class));
    }
}

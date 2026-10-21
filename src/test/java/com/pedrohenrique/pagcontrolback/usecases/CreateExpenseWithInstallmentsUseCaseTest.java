
package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.command.CreateExpenseCommand;
import com.pedrohenrique.pagcontrolback.exceptions.*;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateExpenseWithInstallmentsUseCaseTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CreditCardRepository creditCardRepository;

    @InjectMocks
    private CreateExpenseWithInstallmentsUseCase useCase;

    private User createUser() {
        return new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "$2a$10$abcdefghijklmnopqrstuv",
                "11999999999",
                PersonType.PF
        );
    }

    private Supplier createSupplier(User user) {
        return new Supplier(
                "Supplier Inc.",
                null,
                user
        );
    }

    private CreateExpenseCommand createBillCommand(
            UUID supplierId,
            UUID categoryId
    ) {
        Map<Integer, String> installments = new HashMap<>();
        installments.put(30, null);
        installments.put(60, null);
        installments.put(90, "123456789");

        return new CreateExpenseCommand(
                "INV123",
                "Conta do mercado",
                PaymentType.BILL,
                supplierId,
                LocalDate.now(),
                installments,
                null,
                null,
                new BigDecimal("300.00"),
                categoryId
        );
    }

    private CreateExpenseCommand createCreditCommand(UUID creditCardId) {
        return new CreateExpenseCommand(
                "INV123",
                "Compra no cartão",
                PaymentType.CREDIT,
                null,
                LocalDate.now(),
                null,
                1,
                creditCardId,
                new BigDecimal("100.00"),
                null
        );
    }

    @Nested
    class SuccessTests {

        @Test
        void shouldCreateExpenseWithInstallmentsSuccessfully() {
            UUID authenticatedUserId = UUID.randomUUID();
            UUID supplierId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            User user = createUser();
            Supplier supplier = createSupplier(user);

            Category category = new Category(
                    "Alimentação",
                    TransactionType.EXPENSE,
                    user
            );

            CreateExpenseCommand command =
                    createBillCommand(supplierId, categoryId);

            when(userRepository.getReferenceById(authenticatedUserId))
                    .thenReturn(user);

            when(supplierRepository.findById(supplierId))
                    .thenReturn(Optional.of(supplier));

            when(categoryRepository.findCategoryByIdAndUserId(
                    categoryId,
                    authenticatedUserId
            )).thenReturn(Optional.of(category));

            useCase.execute(authenticatedUserId, command);

            verify(expenseRepository).save(any(Expense.class));
        }
    }

    @Nested
    class ValidationTests {

        @Test
        void shouldThrowWhenCommandIsNull() {
            assertThrows(
                    CreateExpenseCommandRequiredException.class,
                    () -> useCase.execute(UUID.randomUUID(), null)
            );

            verifyNoInteractions(userRepository);
        }

        @Test
        void shouldThrowWhenAuthenticatedUserIdIsNull() {
            CreateExpenseCommand command = createBillCommand(
                    UUID.randomUUID(),
                    null
            );

            assertThrows(
                    UserIdRequiredException.class,
                    () -> useCase.execute(null, command)
            );

            verifyNoInteractions(userRepository);
        }
    }

    @Nested
    class SupplierTests {

        @Test
        void shouldThrowWhenSupplierNotFound() {
            UUID authenticatedUserId = UUID.randomUUID();
            UUID supplierId = UUID.randomUUID();

            User user = createUser();
            CreateExpenseCommand command = createBillCommand(
                    supplierId,
                    null
            );

            when(userRepository.getReferenceById(authenticatedUserId))
                    .thenReturn(user);

            when(supplierRepository.findById(supplierId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    SupplierNotFoundException.class,
                    () -> useCase.execute(authenticatedUserId, command)
            );

            verify(expenseRepository, never()).save(any(Expense.class));
        }
    }

    @Nested
    class CategoryTests {

        @Test
        void shouldThrowWhenCategoryNotFound() {
            UUID authenticatedUserId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            User user = createUser();
            CreateExpenseCommand command = createBillCommand(
                    null,
                    categoryId
            );

            when(userRepository.getReferenceById(authenticatedUserId))
                    .thenReturn(user);

            when(categoryRepository.findCategoryByIdAndUserId(
                    categoryId,
                    authenticatedUserId
            )).thenReturn(Optional.empty());

            assertThrows(
                    CategoryNotFoundException.class,
                    () -> useCase.execute(authenticatedUserId, command)
            );

            verify(expenseRepository, never()).save(any(Expense.class));
        }

        @Test
        void shouldThrowWhenCategoryTypeIsInvalid() {
            UUID authenticatedUserId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            User user = createUser();

            Category category = new Category(
                    "Salário",
                    TransactionType.INCOME,
                    user
            );

            CreateExpenseCommand command = createBillCommand(
                    null,
                    categoryId
            );

            when(userRepository.getReferenceById(authenticatedUserId))
                    .thenReturn(user);

            when(categoryRepository.findCategoryByIdAndUserId(
                    categoryId,
                    authenticatedUserId
            )).thenReturn(Optional.of(category));

            assertThrows(
                    CategoryTypeInvalidException.class,
                    () -> useCase.execute(authenticatedUserId, command)
            );

            verify(expenseRepository, never()).save(any(Expense.class));
        }
    }

    @Nested
    class CreditCardTests {

        @Test
        void shouldThrowWhenCreditCardNotFound() {
            UUID authenticatedUserId = UUID.randomUUID();
            UUID creditCardId = UUID.randomUUID();

            User user = createUser();
            CreateExpenseCommand command = createCreditCommand(creditCardId);

            when(userRepository.getReferenceById(authenticatedUserId))
                    .thenReturn(user);

            when(creditCardRepository.findCreditCardByCardIdAndUserId(
                    eq(creditCardId),
                    any()
            )).thenReturn(null);

            assertThrows(
                    CreditCardNotFoundException.class,
                    () -> useCase.execute(authenticatedUserId, command)
            );

            verify(expenseRepository, never()).save(any(Expense.class));
        }

        @Test
        void shouldThrowWhenCreditCardIsInactive() {
            UUID authenticatedUserId = UUID.randomUUID();
            UUID creditCardId = UUID.randomUUID();

            User user = createUser();
            CreditCard creditCard = mock(CreditCard.class);

            CreateExpenseCommand command = createCreditCommand(creditCardId);

            when(userRepository.getReferenceById(authenticatedUserId))
                    .thenReturn(user);

            when(creditCardRepository.findCreditCardByCardIdAndUserId(
                    eq(creditCardId),
                    any()
            )).thenReturn(creditCard);

            when(creditCard.isActive()).thenReturn(false);

            assertThrows(
                    CreditCardInactiveException.class,
                    () -> useCase.execute(authenticatedUserId, command)
            );

            verify(expenseRepository, never()).save(any(Expense.class));
        }

        @Test
        void shouldCreateExpenseWhenCreditCardIsActive() {
            UUID authenticatedUserId = UUID.randomUUID();
            UUID creditCardId = UUID.randomUUID();

            User user = createUser();
            CreditCard creditCard = new CreditCard(
                "teste",
                9,
                15,
                Money.of(BigDecimal.valueOf(10000.00)),
                user
            );

            CreateExpenseCommand command = createCreditCommand(creditCardId);

            when(userRepository.getReferenceById(authenticatedUserId))
                    .thenReturn(user);

            when(creditCardRepository.findCreditCardByCardIdAndUserId(
                    eq(creditCardId),
                    any()
            )).thenReturn(creditCard);

            useCase.execute(authenticatedUserId, command);

            verify(expenseRepository).save(any(Expense.class));
        }
    }
}

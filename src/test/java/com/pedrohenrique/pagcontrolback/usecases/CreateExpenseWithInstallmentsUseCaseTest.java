package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.command.CreateExpenseCommand;
import com.pedrohenrique.pagcontrolback.exceptions.*;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
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

            Map<Integer, String> installments = new HashMap<>();
            installments.put(30, null);
            installments.put(60, null);
            installments.put(90, "123456789");

            CreateExpenseCommand command = new CreateExpenseCommand(
                    "INV123",
                    "Conta do mercado",
                    PaymentType.CREDIT,
                    supplierId,
                    LocalDate.now(),
                    installments,
                    new BigDecimal("300.00"),
                    categoryId,
                    false,
                    null,
                    null,
                    null
            );

            when(userRepository.getReferenceById(authenticatedUserId))
                    .thenReturn(user);

            when(supplierRepository.findById(supplierId))
                    .thenReturn(Optional.of(supplier));

            when(categoryRepository.findCategoryByIdAndUserId(categoryId, authenticatedUserId))
                    .thenReturn(Optional.of(category));

            useCase.execute(authenticatedUserId, command);

            verify(expenseRepository, times(1))
                    .save(any(Expense.class));
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
        }

        @Test
        void shouldThrowWhenAuthenticatedUserIdIsNull() {

            CreateExpenseCommand command = new CreateExpenseCommand(
                    "INV123",
                    "Descrição",
                    PaymentType.CREDIT,
                    UUID.randomUUID(),
                    LocalDate.now(),
                    Map.of(30, ""),
                    BigDecimal.valueOf(100),
                    null,
                    false,
                    null,
                    null,
                    null
            );

            assertThrows(
                    UserIdRequiredException.class,
                    () -> useCase.execute(null, command)
            );
        }
    }

    @Nested
    class SupplierTests {

        @Test
        void shouldThrowWhenSupplierNotFound() {

            UUID authenticatedUserId = UUID.randomUUID();
            UUID supplierId = UUID.randomUUID();

            User user = createUser();

            CreateExpenseCommand command = new CreateExpenseCommand(
                    "INV123",
                    "Descrição",
                    PaymentType.CREDIT,
                    supplierId,
                    LocalDate.now(),
                    Map.of(30, ""),
                    BigDecimal.valueOf(100),
                    null,
                    false,
                    null,
                    null,
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
        }
    }

    @Nested
    class CategoryTests {

        @Test
        void shouldThrowWhenCategoryNotFound() {

            UUID authenticatedUserId = UUID.randomUUID();
            UUID supplierId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            User user = createUser();

            Supplier supplier = createSupplier(user);

            CreateExpenseCommand command = new CreateExpenseCommand(
                    "INV123",
                    "Descrição",
                    PaymentType.CREDIT,
                    supplierId,
                    LocalDate.now(),
                    Map.of(30, ""),
                    BigDecimal.valueOf(100),
                    categoryId,
                    false,
                    null,
                    null,
                    null
            );

            when(userRepository.getReferenceById(authenticatedUserId))
                    .thenReturn(user);

            when(supplierRepository.findById(supplierId))
                    .thenReturn(Optional.of(supplier));

            when(categoryRepository.findCategoryByIdAndUserId(categoryId, authenticatedUserId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    CategoryNotFoundException.class,
                    () -> useCase.execute(authenticatedUserId, command)
            );
        }

        @Test
        void shouldThrowWhenCategoryTypeIsInvalid() {

            UUID authenticatedUserId = UUID.randomUUID();
            UUID supplierId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            User user = createUser();

            Supplier supplier = createSupplier(user);

            Category category = new Category(
                    "Salário",
                    TransactionType.INCOME,
                    user
            );

            CreateExpenseCommand command = new CreateExpenseCommand(
                    "INV123",
                    "Descrição",
                    PaymentType.CREDIT,
                    supplierId,
                    LocalDate.now(),
                    Map.of(30, ""),
                    BigDecimal.valueOf(100),
                    categoryId,
                    false,
                    null,
                    null,
                    null
            );

            when(userRepository.getReferenceById(authenticatedUserId))
                    .thenReturn(user);

            when(supplierRepository.findById(supplierId))
                    .thenReturn(Optional.of(supplier));

            when(categoryRepository.findCategoryByIdAndUserId(categoryId, authenticatedUserId))
                    .thenReturn(Optional.of(category));

            assertThrows(
                    CategoryTypeInvalidException.class,
                    () -> useCase.execute(authenticatedUserId, command)
            );
        }
    }

    @Nested
    class RecurrenceTests {

        @Test
        void shouldCreateRecurringExpenseSuccessfully() {

            UUID authenticatedUserId = UUID.randomUUID();
            UUID supplierId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            User user = createUser();
            Supplier supplier = createSupplier(user);
            Category category = new Category("Assinaturas", TransactionType.EXPENSE, user);

            CreateExpenseCommand command = new CreateExpenseCommand(
                    "INV-REC-01",
                    "Netflix",
                    PaymentType.CREDIT,
                    supplierId,
                    LocalDate.now(),
                    Map.of(),
                    new BigDecimal("39.90"),
                    categoryId,
                    true,
                    RecurrenceType.MONTHLY,
                    1,
                    null
            );

            when(userRepository.getReferenceById(authenticatedUserId)).thenReturn(user);
            when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
            when(categoryRepository.findCategoryByIdAndUserId(categoryId, authenticatedUserId))
                    .thenReturn(Optional.of(category));
            when(expenseRepository.save(any(Expense.class))).thenReturn(new Expense(
                    command.invoiceNumber(),
                    command.description(),
                    command.paymentType(),
                    command.date(),
                    user,
                    new Money(command.totalAmount()),
                    command.recurrenceType(),
                    command.recurrenceInterval(),
                    command.recurrenceEndDate()
            ));

            Expense expense = useCase.execute(authenticatedUserId, command);

            verify(expenseRepository, times(1)).save(any(Expense.class));

            assertTrue(expense.getRecurring());
            assertEquals(RecurrenceType.MONTHLY, expense.getRecurrenceType());
            assertEquals(1, expense.getInstallments().size());

        }

        @Test
        void shouldThrowWhenRecurrenceTypeIsNull() {

            UUID authenticatedUserId = UUID.randomUUID();
            User user = createUser();

            CreateExpenseCommand command = new CreateExpenseCommand(
                    "INV-REC-02",
                    "Assinatura sem tipo",
                    PaymentType.CREDIT,
                    null,
                    LocalDate.now(),
                    Map.of(),
                    new BigDecimal("50.00"),
                    null,
                    true,
                    null,
                    1,
                    null
            );

            when(userRepository.getReferenceById(authenticatedUserId)).thenReturn(user);

            assertThrows(
                    RecurrenceTypeRequiredException.class,
                    () -> useCase.execute(authenticatedUserId, command)
            );
        }

        @Test
        void shouldThrowWhenRecurrenceIntervalIsNull() {

            UUID authenticatedUserId = UUID.randomUUID();
            User user = createUser();

            CreateExpenseCommand command = new CreateExpenseCommand(
                    "INV-REC-03",
                    "Assinatura sem intervalo",
                    PaymentType.CREDIT,
                    null,
                    LocalDate.now(),
                    Map.of(),
                    new BigDecimal("50.00"),
                    null,
                    true,
                    RecurrenceType.MONTHLY,
                    null,
                    null
            );

            when(userRepository.getReferenceById(authenticatedUserId)).thenReturn(user);

            assertThrows(
                    RecurrenceIntervalException.class,
                    () -> useCase.execute(authenticatedUserId, command)
            );
        }

        @Test
        void shouldThrowWhenRecurrenceIntervalIsZeroOrNegative() {

            UUID authenticatedUserId = UUID.randomUUID();
            User user = createUser();

            CreateExpenseCommand command = new CreateExpenseCommand(
                    "INV-REC-04",
                    "Assinatura com intervalo inválido",
                    PaymentType.CREDIT,
                    null,
                    LocalDate.now(),
                    Map.of(),
                    new BigDecimal("50.00"),
                    null,
                    true,
                    RecurrenceType.MONTHLY,
                    0,
                    null
            );

            when(userRepository.getReferenceById(authenticatedUserId)).thenReturn(user);

            assertThrows(
                    RecurrenceIntervalException.class,
                    () -> useCase.execute(authenticatedUserId, command)
            );
        }

        @Test
        void shouldThrowWhenRecurrenceEndDateIsBeforeExpenseDate() {

            UUID authenticatedUserId = UUID.randomUUID();
            User user = createUser();

            LocalDate expenseDate = LocalDate.now();

            CreateExpenseCommand command = new CreateExpenseCommand(
                    "INV-REC-05",
                    "Assinatura com fim retroativo",
                    PaymentType.CREDIT,
                    null,
                    expenseDate,
                    Map.of(),
                    new BigDecimal("50.00"),
                    null,
                    true,
                    RecurrenceType.MONTHLY,
                    1,
                    expenseDate.minusDays(1)
            );

            when(userRepository.getReferenceById(authenticatedUserId)).thenReturn(user);

            assertThrows(
                    InvalidRecurrenceEndDateException.class,
                    () -> useCase.execute(authenticatedUserId, command)
            );
        }

        @Test
        void shouldThrowWhenSupplierNotFoundForRecurringExpense() {

            UUID authenticatedUserId = UUID.randomUUID();
            UUID supplierId = UUID.randomUUID();
            User user = createUser();

            CreateExpenseCommand command = new CreateExpenseCommand(
                    "INV-REC-06",
                    "Assinatura",
                    PaymentType.CREDIT,
                    supplierId,
                    LocalDate.now(),
                    Map.of(),
                    new BigDecimal("50.00"),
                    null,
                    true,
                    RecurrenceType.MONTHLY,
                    1,
                    null
            );

            when(userRepository.getReferenceById(authenticatedUserId)).thenReturn(user);
            when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

            assertThrows(
                    SupplierNotFoundException.class,
                    () -> useCase.execute(authenticatedUserId, command)
            );
        }
    }
}
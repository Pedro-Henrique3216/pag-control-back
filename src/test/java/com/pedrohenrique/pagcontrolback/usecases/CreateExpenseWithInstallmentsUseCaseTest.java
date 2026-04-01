package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.CreateExpenseCommand;
import com.pedrohenrique.pagcontrolback.exceptions.*;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
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

    @Test
    void whenInstallmentIntervalsAreProvided_thenCreateExpenseWithInstallments(){

        var userId = UUID.randomUUID();

        var user = new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "password123",
                "12345678900",
                PersonType.PF
        );

        var supplierId = UUID.randomUUID();

        var supplier = new Supplier(
                "Supplier Inc."
        );

        var expense = new Expense(
                "INV123",
                PaymentType.CREDIT,
                LocalDate.now(),
                user,
                supplier
        );

        var category =  new Category(
                "teste",
                CategoryType.EXPENSE,
                user
        );

        Map<Integer, String> installmentBarcodesWithDueInDays = new HashMap<>();
        installmentBarcodesWithDueInDays.put(30, null);
        installmentBarcodesWithDueInDays.put(60, null);
        installmentBarcodesWithDueInDays.put(90, "123456789123");

        var amount = new BigDecimal("300.00");

        CreateExpenseCommand command = new CreateExpenseCommand(
                "INV123",
                PaymentType.CREDIT,
                supplierId,
                LocalDate.now(),
                installmentBarcodesWithDueInDays,
                amount,
                UUID.randomUUID(),
                userId
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));

        when(categoryRepository.findCategoryByIdAndUserId(any(), any()))
                .thenReturn(Optional.of(category));

        Expense expenseSaved = useCase.execute(command);

        verify(expenseRepository, times(1)).save(expense);
    }

    @Test
    void whenUserNotFound_thenThrowUserNotFoundException(){
        var userId = UUID.randomUUID();

        var supplierId = UUID.randomUUID();

        Map<Integer, String> installmentBarcodesWithDueInDays = new HashMap<>();
        installmentBarcodesWithDueInDays.put(30, null);
        installmentBarcodesWithDueInDays.put(60, null);
        installmentBarcodesWithDueInDays.put(90, "123456789123");

        var amount = new BigDecimal("300.00");

        CreateExpenseCommand command = new CreateExpenseCommand(
                "INV123",
                PaymentType.CREDIT,
                supplierId,
                LocalDate.now(),
                installmentBarcodesWithDueInDays,
                amount,
                null,
                userId
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());


        assertThrows(UserNotFoundException.class,
                () -> useCase.execute(command));

        verify(expenseRepository, never()).save(any());
    }

    @Test
    void whenInstallmentIntervalsAreEmpty_thenThrowInstallmentsRequiredForPaymentTypeException(){

        var userId = UUID.randomUUID();

        var user = new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "password123",
                "12345678900",
                PersonType.PF
        );

        var supplierId = UUID.randomUUID();

        var supplier = new Supplier(
                "Supplier Inc."
        );

        var amount = new BigDecimal("300.00");

        CreateExpenseCommand command = new CreateExpenseCommand(
                "INV123",
                PaymentType.CREDIT,
                supplierId,
                LocalDate.now(),
                null,
                amount,
                null,
                userId
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));


        assertThrows(InstallmentsRequiredForPaymentTypeException.class,
                () -> useCase.execute(command));

        verify(expenseRepository, never()).save(any());
    }

    @Test
    void whenSupplierNotFound_thenThrowSupplierNotFoundException(){
        var userId = UUID.randomUUID();

        var user = new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "password123",
                "12345678900",
                PersonType.PF
        );

        var supplierId = UUID.randomUUID();

        Map<Integer, String> installmentBarcodesWithDueInDays = new HashMap<>();
        installmentBarcodesWithDueInDays.put(30, null);
        installmentBarcodesWithDueInDays.put(60, null);
        installmentBarcodesWithDueInDays.put(90, "123456789123");

        var amount = new BigDecimal("300.00");

        CreateExpenseCommand command = new CreateExpenseCommand(
                "INV123",
                PaymentType.CREDIT,
                supplierId,
                LocalDate.now(),
                installmentBarcodesWithDueInDays,
                amount,
                null,
                userId
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.empty());

        assertThrows(SupplierNotFoundException.class,
                () -> useCase.execute(command));

        verify(expenseRepository, never()).save(any());
    }

    @Test
    void whenPaymentTypeIsCashAndMoreThanOneInstallmentProvided_thenThrowMultipleInstallmentsNotAllowedException(){
        var userId = UUID.randomUUID();

        var user = new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "password123",
                "12345678900",
                PersonType.PF
        );

        var supplierId = UUID.randomUUID();

        var supplier = new Supplier(
                "Supplier Inc."
        );

        Map<Integer, String> installmentBarcodesWithDueInDays = new HashMap<>();
        installmentBarcodesWithDueInDays.put(30, null);
        installmentBarcodesWithDueInDays.put(60, null);
        installmentBarcodesWithDueInDays.put(90, "123456789123");

        var amount = new BigDecimal("300.00");

        CreateExpenseCommand command = new CreateExpenseCommand(
                "INV123",
                PaymentType.CASH,
                supplierId,
                LocalDate.now(),
                installmentBarcodesWithDueInDays,
                amount,
                null,
                userId
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));


        assertThrows(MultipleInstallmentsNotAllowedForPaymentTypeException.class, () ->
                useCase.execute(command)
        );

        verify(expenseRepository, never()).save(any());
    }

    @Test
    void whenPaymentTypeIsCash_thenCreateSingleInstallment(){
        var userId = UUID.randomUUID();

        var user = new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "password123",
                "12345678900",
                PersonType.PF
        );

        var supplierId = UUID.randomUUID();

        var supplier = new Supplier(
                "Supplier Inc."
        );

        var expense = new Expense(
                "INV123",
                PaymentType.CASH,
                LocalDate.now(),
                user,
                supplier
        );

        Map<Integer, String> installmentBarcodesWithDueInDays = new HashMap<>();
        installmentBarcodesWithDueInDays.put(0, "123456789123");

        var amount = new BigDecimal("300.00");

        CreateExpenseCommand command = new CreateExpenseCommand(
                "INV123",
                PaymentType.CASH,
                supplierId,
                LocalDate.now(),
                installmentBarcodesWithDueInDays,
                amount,
                null,
                userId
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));


        useCase.execute(command);

        verify(expenseRepository, times(1)).save(expense);
    }

    @Test
    void whenInstallmentDueInDaysIsInvalid_thenThrowInvalidInstallmentDueInDaysException() {

        var userId = UUID.randomUUID();

        var user = new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "password123",
                "12345678900",
                PersonType.PF
        );

        var supplierId = UUID.randomUUID();

        var supplier = new Supplier(
                "Supplier Inc."
        );

        Map<Integer, String> installmentBarcodesWithDueInDays = new HashMap<>();
        installmentBarcodesWithDueInDays.put(-5, null);

        var amount = new BigDecimal("300.00");

        CreateExpenseCommand command = new CreateExpenseCommand(
                "INV123",
                PaymentType.CASH,
                supplierId,
                LocalDate.now(),
                installmentBarcodesWithDueInDays,
                amount,
                null,
                userId
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));


        assertThrows(InvalidInstallmentDueInDaysException.class, () ->
                useCase.execute(command)
        );

        verify(expenseRepository, never()).save(any());
    }

    @Test
    void whenPaymentTypeIsCashAndBarcodeMapIsNull_thenCreateSingleInstallment() {
        var userId = UUID.randomUUID();

        var user = new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "password123",
                "12345678900",
                PersonType.PF
        );

        var supplierId = UUID.randomUUID();

        var supplier = new Supplier(
                "Supplier Inc."
        );

        var expense = new Expense(
                "INV123",
                PaymentType.CASH,
                LocalDate.now(),
                user,
                supplier
        );

        var amount = new BigDecimal("300.00");

        CreateExpenseCommand command = new CreateExpenseCommand(
                "INV123",
                PaymentType.CASH,
                supplierId,
                LocalDate.now(),
                null,
                amount,
                null,
                userId
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));

        when(expenseRepository.save(expense))
                .thenReturn(expense);

        useCase.execute(command);

        verify(expenseRepository, times(1)).save(expense);
    }

    @Test
    void shouldThrowExceptionWhenBarcodeInstallmentDueInDaysIsNotZero(){
        var userId = UUID.randomUUID();

        var user = new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "password123",
                "12345678900",
                PersonType.PF
        );

        var supplierId = UUID.randomUUID();

        var supplier = new Supplier(
                "Supplier Inc."
        );

        Map<Integer, String> installmentBarcodesWithDueInDays = new HashMap<>();
        installmentBarcodesWithDueInDays.put(1, "123456789123");

        var amount = new BigDecimal("300.00");

        CreateExpenseCommand command = new CreateExpenseCommand(
                "INV123",
                PaymentType.CASH,
                supplierId,
                LocalDate.now(),
                installmentBarcodesWithDueInDays,
                amount,
                null,
                userId
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));



        assertThrows(InvalidInstallmentDueInDaysException.class, () ->
                useCase.execute(command)
        );

    }

    @Test
    void shouldThrowCategoryNotFoundExceptionWhenCategoryDoesNotExist(){
        var userId = UUID.randomUUID();

        var user = new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "password123",
                "12345678900",
                PersonType.PF
        );

        var supplierId = UUID.randomUUID();

        var supplier = new Supplier(
                "Supplier Inc."
        );


        Map<Integer, String> installmentBarcodesWithDueInDays = new HashMap<>();
        installmentBarcodesWithDueInDays.put(30, null);
        installmentBarcodesWithDueInDays.put(60, null);
        installmentBarcodesWithDueInDays.put(90, "123456789123");

        var amount = new BigDecimal("300.00");

        CreateExpenseCommand command = new CreateExpenseCommand(
                "INV123",
                PaymentType.CREDIT,
                supplierId,
                LocalDate.now(),
                installmentBarcodesWithDueInDays,
                amount,
                UUID.randomUUID(),
                userId
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));

        when(categoryRepository.findCategoryByIdAndUserId(any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> useCase.execute(command)
        );

    }

    @Test
    void shouldThrowCategoryTypeInvalidExceptionWhenCategoryIsNotExpenseType(){
        var userId = UUID.randomUUID();

        var user = new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "password123",
                "12345678900",
                PersonType.PF
        );

        var supplierId = UUID.randomUUID();

        var supplier = new Supplier(
                "Supplier Inc."
        );

        var category =  new Category(
                "teste",
                CategoryType.INCOME,
                user
        );

        Map<Integer, String> installmentBarcodesWithDueInDays = new HashMap<>();
        installmentBarcodesWithDueInDays.put(30, null);
        installmentBarcodesWithDueInDays.put(60, null);
        installmentBarcodesWithDueInDays.put(90, "123456789123");

        var amount = new BigDecimal("300.00");

        CreateExpenseCommand command = new CreateExpenseCommand(
                "INV123",
                PaymentType.CREDIT,
                supplierId,
                LocalDate.now(),
                installmentBarcodesWithDueInDays,
                amount,
                UUID.randomUUID(),
                userId
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));

        when(categoryRepository.findCategoryByIdAndUserId(any(), any()))
                .thenReturn(Optional.of(category));

        assertThrows(
                CategoryTypeInvalidException.class,
                () -> useCase.execute(command)
        );
    }


}
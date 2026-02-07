package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.*;
import com.pedrohenrique.pagcontrolback.model.*;
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
                LocalDate.now()
        );

        Map<Integer, String> installmentBarcodesWithDueInDays = new HashMap<>();
        installmentBarcodesWithDueInDays.put(30, null);
        installmentBarcodesWithDueInDays.put(60, null);
        installmentBarcodesWithDueInDays.put(90, "123456789123");

        var amount = new BigDecimal("300.00");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));

        when(expenseRepository.save(expense))
                .thenReturn(expense);

        Expense expenseSaved = useCase.execute(userId, supplierId, expense, installmentBarcodesWithDueInDays, amount);

        verify(expenseRepository, times(1)).save(expense);

        assertEquals(3, expenseSaved.getInstallments().size());
        assertEquals(100.0, expenseSaved.getInstallments().get(0).getAmount().doubleValue());
    }

    @Test
    void whenUserNotFound_thenThrowUserNotFoundException(){
        var userId = UUID.randomUUID();

        var supplierId = UUID.randomUUID();

        var expense = new Expense(
                "INV123",
                PaymentType.CREDIT,
                LocalDate.now()
        );

        Map<Integer, String> installmentBarcodesWithDueInDays = new HashMap<>();
        installmentBarcodesWithDueInDays.put(30, null);
        installmentBarcodesWithDueInDays.put(60, null);
        installmentBarcodesWithDueInDays.put(90, "123456789123");

        var amount = new BigDecimal("300.00");

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());


        assertThrows(UserNotFoundException.class,
                () -> useCase.execute(userId, supplierId, expense, installmentBarcodesWithDueInDays, amount));

        verify(expenseRepository, never()).save(expense);
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

        var expense = new Expense(
                "INV123",
                PaymentType.CREDIT,
                LocalDate.now()
        );

        var amount = new BigDecimal("300.00");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));


        assertThrows(InstallmentsRequiredForPaymentTypeException.class,
                () -> useCase.execute(userId, supplierId, expense, null, amount));

        verify(expenseRepository, never()).save(expense);
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

        var expense = new Expense(
                "INV123",
                PaymentType.CREDIT,
                LocalDate.now()
        );

        Map<Integer, String> installmentBarcodesWithDueInDays = new HashMap<>();
        installmentBarcodesWithDueInDays.put(30, null);
        installmentBarcodesWithDueInDays.put(60, null);
        installmentBarcodesWithDueInDays.put(90, "123456789123");

        var amount = new BigDecimal("300.00");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.empty());

        assertThrows(SupplierNotFoundException.class,
                () -> useCase.execute(userId, supplierId, expense, installmentBarcodesWithDueInDays, amount));

        verify(expenseRepository, never()).save(expense);
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

        var expense = new Expense(
                "INV123",
                PaymentType.CASH,
                LocalDate.now()
        );

        Map<Integer, String> installmentBarcodesWithDueInDays = new HashMap<>();
        installmentBarcodesWithDueInDays.put(30, null);
        installmentBarcodesWithDueInDays.put(60, null);
        installmentBarcodesWithDueInDays.put(90, "123456789123");

        var amount = new BigDecimal("300.00");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));


        assertThrows(MultipleInstallmentsNotAllowedForPaymentTypeException.class, () ->
                useCase.execute(userId, supplierId, expense, installmentBarcodesWithDueInDays, amount)
        );

        verify(expenseRepository, never()).save(expense);
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
                LocalDate.now()
        );

        Map<Integer, String> installmentBarcodesWithDueInDays = new HashMap<>();
        installmentBarcodesWithDueInDays.put(0, "123456789123");

        var amount = new BigDecimal("300.00");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));

        when(expenseRepository.save(expense))
                .thenReturn(expense);

        Expense expenseSaved = useCase.execute(userId, supplierId, expense, installmentBarcodesWithDueInDays, amount);

        verify(expenseRepository, times(1)).save(expense);

        assertEquals(1, expenseSaved.getInstallments().size());
        assertEquals(300.0, expenseSaved.getInstallments().get(0).getAmount().doubleValue());
        assertEquals("123456789123", expenseSaved.getInstallments().get(0).getBarcode());
        assertEquals(LocalDate.now(), expenseSaved.getInstallments().get(0).getDueDate());
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

        var expense = new Expense(
                "INV123",
                PaymentType.CREDIT,
                LocalDate.now()
        );

        Map<Integer, String> installmentBarcodesWithDueInDays = new HashMap<>();
        installmentBarcodesWithDueInDays.put(-5, null);

        var amount = new BigDecimal("300.00");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));


        assertThrows(InvalidInstallmentDueInDaysException.class, () ->
                useCase.execute(userId, supplierId, expense, installmentBarcodesWithDueInDays, amount)
        );

        verify(expenseRepository, never()).save(expense);
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
                LocalDate.now()
        );

        var amount = new BigDecimal("300.00");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));

        when(expenseRepository.save(expense))
                .thenReturn(expense);

        Expense expenseSaved = useCase.execute(userId, supplierId, expense, null, amount);

        verify(expenseRepository, times(1)).save(expense);

        assertEquals(1, expenseSaved.getInstallments().size());
        assertEquals(300.0, expenseSaved.getInstallments().get(0).getAmount().doubleValue());
        assertNull(expenseSaved.getInstallments().get(0).getBarcode());
        assertEquals(LocalDate.now(), expenseSaved.getInstallments().get(0).getDueDate());
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

        var expense = new Expense(
                "INV123",
                PaymentType.CASH,
                LocalDate.now()
        );

        var amount = new BigDecimal("300.00");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(supplier));

        Map<Integer, String> installmentBarcodesWithDueInDays = new HashMap<>();
        installmentBarcodesWithDueInDays.put(1, "123456789123");

        assertThrows(InvalidInstallmentDueInDaysException.class, () ->
                useCase.execute(userId, supplierId, expense, installmentBarcodesWithDueInDays, amount)
        );

    }


}
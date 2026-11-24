package com.pedrohenrique.pagcontrolback.mappers;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.response.ExpenseResponseDto;
import com.pedrohenrique.pagcontrolback.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseMapperTest {

    @Test
    void shouldTransformExpenseWithSupplierAndCategory() {
        Expense expense = mock(Expense.class);
        Supplier supplier = mock(Supplier.class);
        Category category = mock(Category.class);
        Installment paidInstallment = mock(Installment.class);
        Installment unpaidInstallment = mock(Installment.class);

        UUID expenseId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        LocalDate date = LocalDate.of(2026, 11, 20);

        when(expense.getId()).thenReturn(expenseId);
        when(expense.getDescription()).thenReturn("Compra de materiais");
        when(expense.getInvoiceNumber()).thenReturn("INV-001");
        when(expense.getPaymentType()).thenReturn(PaymentType.CREDIT);
        when(expense.getTotalAmount()).thenReturn(
                Money.of(new BigDecimal("500.00"))
        );
        when(expense.getExpenseDate()).thenReturn(date);

        when(expense.getSupplier()).thenReturn(supplier);
        when(supplier.getId()).thenReturn(supplierId);
        when(supplier.getName()).thenReturn("Fornecedor teste");

        when(expense.getCategory()).thenReturn(category);
        when(category.getId()).thenReturn(categoryId);
        when(category.getName()).thenReturn("Materiais");

        when(paidInstallment.getStatus()).thenReturn(InstallmentStatus.PAID);
        when(unpaidInstallment.getStatus()).thenReturn(InstallmentStatus.UNPAID);

        when(expense.getInstallments()).thenReturn(
                List.of(paidInstallment, unpaidInstallment)
        );

        ExpenseResponseDto response =
                ExpenseMapper.transformExpenseToExpenseResponseDto(expense);

        assertNotNull(response);
        assertEquals(expenseId, response.id());
        assertEquals("Compra de materiais", response.description());
        assertEquals("INV-001", response.invoiceNumber());
        assertEquals(PaymentType.CREDIT, response.paymentType());
        assertEquals(new BigDecimal("500.00"), response.totalAmount());
        assertEquals(date, response.date());

        assertNotNull(response.supplier());
        assertEquals(supplierId, response.supplier().supplierId());
        assertEquals("Fornecedor teste", response.supplier().supplierName());

        assertNotNull(response.category());
        assertEquals(categoryId, response.category().categoryId());
        assertEquals("Materiais", response.category().categoryName());

        assertEquals(1, response.numberOfInstallmentsPaid());
        assertEquals(2, response.numberOfInstallments());
    }

    @Test
    void shouldTransformExpenseWithoutSupplier() {
        Expense expense = mock(Expense.class);

        when(expense.getId()).thenReturn(UUID.randomUUID());
        when(expense.getDescription()).thenReturn("Despesa sem fornecedor");
        when(expense.getInvoiceNumber()).thenReturn(null);
        when(expense.getPaymentType()).thenReturn(PaymentType.PIX);
        when(expense.getTotalAmount()).thenReturn(
                Money.of(new BigDecimal("120.00"))
        );
        when(expense.getExpenseDate()).thenReturn(LocalDate.now());
        when(expense.getSupplier()).thenReturn(null);
        when(expense.getCategory()).thenReturn(null);
        when(expense.getInstallments()).thenReturn(List.of());

        ExpenseResponseDto response =
                ExpenseMapper.transformExpenseToExpenseResponseDto(expense);

        assertNotNull(response);
        assertNull(response.supplier());
        assertNull(response.category());
        assertEquals(0, response.numberOfInstallmentsPaid());
        assertEquals(0, response.numberOfInstallments());
    }

    @Test
    void shouldCountAllPaidInstallments() {
        Expense expense = mock(Expense.class);

        Installment first = mock(Installment.class);
        Installment second = mock(Installment.class);
        Installment third = mock(Installment.class);

        when(first.getStatus()).thenReturn(InstallmentStatus.PAID);
        when(second.getStatus()).thenReturn(InstallmentStatus.PAID);
        when(third.getStatus()).thenReturn(InstallmentStatus.UNPAID);

        when(expense.getId()).thenReturn(UUID.randomUUID());
        when(expense.getDescription()).thenReturn("Despesa parcelada");
        when(expense.getPaymentType()).thenReturn(PaymentType.CREDIT);
        when(expense.getTotalAmount()).thenReturn(
                Money.of(new BigDecimal("900.00"))
        );
        when(expense.getExpenseDate()).thenReturn(LocalDate.now());
        when(expense.getInstallments()).thenReturn(
                List.of(first, second, third)
        );

        ExpenseResponseDto response =
                ExpenseMapper.transformExpenseToExpenseResponseDto(expense);

        assertEquals(2, response.numberOfInstallmentsPaid());
        assertEquals(3, response.numberOfInstallments());
    }

    @Test
    void shouldReturnZeroPaidInstallmentsWhenNoneArePaid() {
        Expense expense = mock(Expense.class);

        Installment first = mock(Installment.class);
        Installment second = mock(Installment.class);

        when(first.getStatus()).thenReturn(InstallmentStatus.UNPAID);
        when(second.getStatus()).thenReturn(InstallmentStatus.UNPAID);

        when(expense.getId()).thenReturn(UUID.randomUUID());
        when(expense.getDescription()).thenReturn("Despesa pendente");
        when(expense.getPaymentType()).thenReturn(PaymentType.CREDIT);
        when(expense.getTotalAmount()).thenReturn(
                Money.of(new BigDecimal("300.00"))
        );
        when(expense.getExpenseDate()).thenReturn(LocalDate.now());
        when(expense.getInstallments()).thenReturn(List.of(first, second));

        ExpenseResponseDto response =
                ExpenseMapper.transformExpenseToExpenseResponseDto(expense);

        assertEquals(0, response.numberOfInstallmentsPaid());
        assertEquals(2, response.numberOfInstallments());
    }
}

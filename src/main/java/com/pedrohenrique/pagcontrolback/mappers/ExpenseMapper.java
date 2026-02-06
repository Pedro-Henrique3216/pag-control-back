package com.pedrohenrique.pagcontrolback.mappers;

import com.pedrohenrique.pagcontrolback.dtos.request.ExpenseRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.ExpenseResponseDto;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.model.Installment;

import java.math.BigDecimal;

public class ExpenseMapper {

    public static Expense toDomain(ExpenseRequestDto expenseRequestDto) {
        return new Expense(
                expenseRequestDto.invoiceNumber(),
                expenseRequestDto.paymentType(),
                expenseRequestDto.date()
        );
    }

    public static ExpenseResponseDto fromDomain(Expense expense) {
        return new ExpenseResponseDto(
                expense.getId(),
                expense.getInvoiceNumber(),
                expense.getPaymentType(),
                expense.getSupplier().getId(),
                expense.getExpenseDate(),
                expense.getInstallments()
                        .stream()
                        .map(Installment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                expense.getInstallments()
                        .stream()
                        .map(InstallmentMapper::fromDomain)
                        .toList()
        );
    }
}

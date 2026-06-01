package com.pedrohenrique.pagcontrolback.mappers;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.response.ExpenseResponseDto;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.model.Installment;

import java.math.BigDecimal;

public class ExpenseMapper {

    public static ExpenseResponseDto fromDomain(Expense expense) {
        return new ExpenseResponseDto(
                expense.getId(),
                expense.getInvoiceNumber(),
                expense.getDescription(),
                expense.getPaymentType(),
                expense.getSupplier() == null ? null : expense.getSupplier().getId(),
                expense.getExpenseDate(),
                expense.getTotalAmount().value(),
                expense.getCategory() == null ? null : expense.getCategory().getId(),
                expense.getInstallments()
                        .stream()
                        .map(InstallmentMapper::fromDomain)
                        .toList()
        );
    }
}

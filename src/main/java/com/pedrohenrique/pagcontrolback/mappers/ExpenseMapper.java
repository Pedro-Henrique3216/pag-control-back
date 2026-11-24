package com.pedrohenrique.pagcontrolback.mappers;

import com.pedrohenrique.pagcontrolback.dtos.helper.CategoryDto;
import com.pedrohenrique.pagcontrolback.dtos.helper.SupplierDto;
import com.pedrohenrique.pagcontrolback.dtos.response.ExpenseResponseDto;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.model.InstallmentStatus;
import com.pedrohenrique.pagcontrolback.model.Supplier;

public class ExpenseMapper {

    public static ExpenseResponseDto transformExpenseToExpenseResponseDto(Expense expense) {

        Supplier supplier = expense.getSupplier();
        Category category = expense.getCategory();
        int installmentsPaid = (int) expense.getInstallments()
                .stream()
                .filter(installment ->
                        installment.getStatus() == InstallmentStatus.PAID
                )
                .count();


        return new ExpenseResponseDto(
                expense.getId(),
                expense.getDescription(),
                expense.getInvoiceNumber(),
                expense.getPaymentType(),
                expense.getTotalAmount().value(),
                expense.getExpenseDate(),
                supplier == null ? null : new SupplierDto(supplier.getId(), supplier.getName()),
                category == null ? null : new CategoryDto(category.getId(), category.getName()),
                installmentsPaid,
                expense.getInstallments().size()
        );
    }
}

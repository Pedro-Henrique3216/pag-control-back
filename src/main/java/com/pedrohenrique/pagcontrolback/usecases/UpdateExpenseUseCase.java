package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.UpdateExpenseCommand;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.ExpenseNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateExpenseUseCase {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public UpdateExpenseUseCase(
            ExpenseRepository expenseRepository,
            CategoryRepository categoryRepository,
            SupplierRepository supplierRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    @Transactional
    public Expense updateExpense(
            UUID expenseId,
            UUID userId,
            UpdateExpenseCommand command
    ) {
        if (expenseId == null) {
            throw new IllegalArgumentException("Expense id cannot be null");
        }

        if (userId == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }

        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }

        Expense expense = expenseRepository
                .findActiveExpenseByIdAndUserId(expenseId, userId)
                .orElseThrow(() ->
                        new ExpenseNotFoundException("Expense not found")
                );

        Supplier supplier = null;

        if (command.supplierId() != null) {
            supplier = supplierRepository
                    .findByIdAndUser_Id(command.supplierId(), userId)
                    .orElseThrow(() ->
                            new SupplierNotFoundException("Supplier not found")
                    );
        }

        Category category = null;

        if (command.categoryId() != null) {
            category = categoryRepository
                    .findCategoryByIdAndUserId(command.categoryId(), userId)
                    .orElseThrow(() ->
                            new CategoryNotFoundException("Category not found")
                    );
        }

        expense.updateDetails(
                command.description(),
                command.invoiceNumber(),
                supplier,
                category
        );

        return expenseRepository.save(expense);
    }
}
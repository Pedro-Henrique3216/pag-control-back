package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.CreateExpenseCommand;
import com.pedrohenrique.pagcontrolback.exceptions.*;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CreateExpenseWithInstallmentsUseCase {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;

    public CreateExpenseWithInstallmentsUseCase(
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            SupplierRepository supplierRepository,
            CategoryRepository categoryRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.supplierRepository = supplierRepository;
        this.categoryRepository = categoryRepository;
    }

    public Expense execute(CreateExpenseCommand command) {

        if (command == null) {
            throw new CreateExpenseCommandRequiredException("Create expense command is required");
        }

        if (command.totalAmount() == null || command.totalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidExpenseAmountException("Total amount must be greater than zero.");
        }

        if (command.userId() == null) {
            throw new UserIdRequiredException("User ID is required.");
        }

        if (command.supplierId() == null) {
            throw new SupplierRequiredException("Supplier ID is required.");
        }

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + command.userId()));

        Supplier supplier = supplierRepository.findById(command.supplierId())
                        .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + command.supplierId()));

        Expense expense = new Expense(
                command.invoiceNumber(),
                command.paymentType(),
                command.date(),
                user,
                supplier
        );

        if(command.categoryId() != null) {
            Category category = categoryRepository.findCategoryByIdAndUserId(command.categoryId(), command.userId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + command.categoryId()));
            expense.assignCategory(category);
        }

        expense.generateInstallments(
                command.totalAmount(),
                command.barcodeByDueInDays()
        );

        return expenseRepository.save(expense);
    }

}
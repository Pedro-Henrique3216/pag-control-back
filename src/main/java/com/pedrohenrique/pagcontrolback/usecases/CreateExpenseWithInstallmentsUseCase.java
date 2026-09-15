package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

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

    @Transactional
    public Expense execute(UUID authenticatedUserId, CreateExpenseCommand command) {

        if (command == null) {
            throw new CreateExpenseCommandRequiredException("Create expense command is required");
        }

        if (authenticatedUserId == null) {
            throw new UserIdRequiredException("User ID is required.");
        }

        User user = userRepository.getReferenceById(authenticatedUserId);

        Expense expense = command.isRecurring() ? buildRecurringExpense(command, user) : buildRegularExpense(command, user);

        if (command.supplierId() != null) {
            Supplier supplier = supplierRepository.findById(command.supplierId())
                    .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + command.supplierId()));
            expense.setSupplier(supplier);
        }

        if(command.categoryId() != null) {
            Category category = categoryRepository.findCategoryByIdAndUserId(command.categoryId(), authenticatedUserId)
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + command.categoryId()));
            expense.assignCategory(category);
        }

        return expenseRepository.save(expense);
    }

    private Expense buildRecurringExpense(CreateExpenseCommand command, User user) {
        return new Expense(
                command.invoiceNumber(),
                command.description(),
                command.paymentType(),
                command.date(),
                user,
                new Money(command.totalAmount()),
                command.recurrenceType(),
                command.recurrenceInterval(),
                command.recurrenceEndDate()
        );

    }

    private Expense buildRegularExpense(CreateExpenseCommand command, User user) {
        Expense expense = new Expense(
                command.invoiceNumber(),
                command.description(),
                command.paymentType(),
                command.date(),
                user,
                new Money(command.totalAmount())
        );
        expense.generateInstallments(command.barcodeByDueInDays());
        return expense;
    }

}
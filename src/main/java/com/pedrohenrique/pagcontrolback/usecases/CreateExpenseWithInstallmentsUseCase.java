package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.command.CreateExpenseCommand;
import com.pedrohenrique.pagcontrolback.exceptions.*;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateExpenseWithInstallmentsUseCase {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final CreditCardRepository creditCardRepository;

    public CreateExpenseWithInstallmentsUseCase(
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            SupplierRepository supplierRepository,
            CategoryRepository categoryRepository,
            CreditCardRepository creditCardRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.supplierRepository = supplierRepository;
        this.categoryRepository = categoryRepository;
        this.creditCardRepository = creditCardRepository;
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

        Expense expense = buildRegularExpense(command, user);

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

    private Expense buildRegularExpense(CreateExpenseCommand command, User user) {

        if (command.paymentType() == PaymentType.CREDIT) {
            CreditCard creditCard = creditCardRepository.findCreditCardByCardIdAndUserId(command.creditCardId(), user.getId());
            if (creditCard == null) {
                throw new CreditCardNotFoundException("Credit card not found with id: " + command.creditCardId());
            }

            if (!creditCard.isActive()){
                throw new CreditCardInactiveException("Credit card is not active.");
            }

            Expense expense = new Expense(
                    command.invoiceNumber(),
                    command.description(),
                    command.paymentType(),
                    command.date(),
                    user,
                    Money.of(command.totalAmount()),
                    creditCard
            );
            expense.generateCreditCardInstallments(command.numberOfInstallments());
            return expense;

        } else if (command.paymentType() == PaymentType.BILL) {
            Expense expense = new Expense(
                    command.invoiceNumber(),
                    command.description(),
                    command.paymentType(),
                    command.date(),
                    user,
                    Money.of(command.totalAmount())
            );
            expense.generateBillInstallments(command.barcodeByDueInDays());
            return expense;

        } else {
            Expense expense = new Expense(
                    command.invoiceNumber(),
                    command.description(),
                    command.paymentType(),
                    command.date(),
                    user,
                    Money.of(command.totalAmount())
            );
            expense.generateSingleInstallment();
            return expense;
        }
    }

}
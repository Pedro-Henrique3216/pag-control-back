
package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.command.CreateRecurringExpenseCommand;
import com.pedrohenrique.pagcontrolback.exceptions.*;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateRecurringExpenseUseCase {

    private final RecurringExpenseRepository recurringExpenseRepository;
    private final UserRepository userRepository;
    private final CreditCardRepository creditCardRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;

    public CreateRecurringExpenseUseCase(
            RecurringExpenseRepository recurringExpenseRepository,
            UserRepository userRepository,
            CreditCardRepository creditCardRepository,
            SupplierRepository supplierRepository,
            CategoryRepository categoryRepository
    ) {
        this.recurringExpenseRepository = recurringExpenseRepository;
        this.userRepository = userRepository;
        this.creditCardRepository = creditCardRepository;
        this.supplierRepository = supplierRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public RecurringExpense execute(
            CreateRecurringExpenseCommand command,
            UUID authenticatedUserId
    ) {
        if (command == null) {
            throw new CreateExpenseCommandRequiredException(
                    "Create expense command is required."
            );
        }

        if (authenticatedUserId == null) {
            throw new UserIdRequiredException(
                    "User ID is required."
            );
        }

        User user = userRepository.getReferenceById(
                authenticatedUserId
        );

        Supplier supplier = null;

        if (command.supplierId() != null) {
            supplier = supplierRepository
                    .findById(command.supplierId())
                    .orElseThrow(() ->
                            new SupplierNotFoundException(
                                    "Supplier not found with id: "
                                            + command.supplierId()
                            )
                    );
        }

        Category category = null;

        if (command.categoryId() != null) {
            category = categoryRepository
                    .findCategoryByIdAndUserId(
                            command.categoryId(),
                            authenticatedUserId
                    )
                    .orElseThrow(() ->
                            new CategoryNotFoundException(
                                    "Category not found with id: "
                                            + command.categoryId()
                            )
                    );
        }

        CreditCard creditCard = null;

        if (command.paymentType() == PaymentType.CREDIT) {
            if (command.creditCardId() == null) {
                throw new CreditCardRequiredException(
                        "Credit card ID is required."
                );
            }

            creditCard = creditCardRepository
                    .findCreditCardByCardIdAndUserId(
                            command.creditCardId(),
                            authenticatedUserId
                    );

            if (creditCard == null) {
                throw new CreditCardNotFoundException(
                        "Credit card not found."
                );
            }
        }

        RecurringExpense recurringExpense = new RecurringExpense(
                command.description(),
                Money.of(command.totalAmount()),
                command.paymentType(),
                command.recurrenceType(),
                command.recurrenceInterval(),
                command.recurrenceDay(),
                command.startDate(),
                command.recurrenceEndDate(),
                user,
                supplier,
                category,
                creditCard
        );

        return recurringExpenseRepository.save(recurringExpense);
    }
}

package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.command.UpdateRecurringExpenseCommand;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.RecurringExpenseNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierNotFoundException;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.CreditCardRepository;
import com.pedrohenrique.pagcontrolback.repositories.RecurringExpenseRepository;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateRecurringExpenseUseCase {

    private final RecurringExpenseRepository recurringExpenseRepository;
    private final CreditCardRepository creditCardRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;

    public UpdateRecurringExpenseUseCase(
            RecurringExpenseRepository recurringExpenseRepository,
            CreditCardRepository creditCardRepository,
            SupplierRepository supplierRepository,
            CategoryRepository categoryRepository
    ) {
        this.recurringExpenseRepository = recurringExpenseRepository;
        this.creditCardRepository = creditCardRepository;
        this.supplierRepository = supplierRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public RecurringExpense updateRecurringExpenseById(UUID recurringExpenseId, UUID userId, UpdateRecurringExpenseCommand command){
        RecurringExpense recurringExpense = recurringExpenseRepository.findRecurringExpenseByIdAndUserId(recurringExpenseId, userId)
                .orElseThrow(() -> new RecurringExpenseNotFoundException("recurring expense id not found"));

        CreditCard creditCard = null;
        Supplier supplier = null;
        Category category = null;

        if (command.paymentType() == PaymentType.CREDIT && command.creditCardId() != null){
            creditCard = creditCardRepository.findCreditCardByCardIdAndUserId(command.creditCardId(), userId);
        }

        if (command.supplierId() != null){
            supplier = supplierRepository.findByIdAndUser_Id(command.supplierId(), userId)
                    .orElseThrow(() -> new SupplierNotFoundException("supplier id not found"));
        }

        if (command.categoryId() != null){
            category = categoryRepository.findCategoryByIdAndUserId(command.categoryId(), userId)
                    .orElseThrow(() -> new CategoryNotFoundException("category id not found"));
        }

        recurringExpense.update(
                command.description(),
                Money.of(command.amount()),
                command.paymentType(),
                supplier,
                category,
                creditCard
        );

        return recurringExpenseRepository.save(recurringExpense);
    }
}

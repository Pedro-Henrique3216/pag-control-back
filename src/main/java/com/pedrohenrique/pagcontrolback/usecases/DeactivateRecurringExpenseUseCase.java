package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.RecurringExpenseNotFoundException;
import com.pedrohenrique.pagcontrolback.model.RecurringExpense;
import com.pedrohenrique.pagcontrolback.repositories.RecurringExpenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeactivateRecurringExpenseUseCase {

    private final RecurringExpenseRepository recurringExpenseRepository;

    public DeactivateRecurringExpenseUseCase(
            RecurringExpenseRepository recurringExpenseRepository
    ) {
        this.recurringExpenseRepository = recurringExpenseRepository;
    }

    @Transactional
    public void deactivateRecurringExpense(UUID recurringExpenseId, UUID userId) {
        if (recurringExpenseId == null){
            throw new IllegalArgumentException("recurring expense id cannot be null");
        }

        if (userId == null){
            throw new IllegalArgumentException("user id cannot be null");
        }

        RecurringExpense recurringExpense = recurringExpenseRepository.findRecurringExpenseByIdAndUserId(recurringExpenseId, userId)
                .orElseThrow(() -> new RecurringExpenseNotFoundException("recurring expense not found"));

        if (!recurringExpense.isActive()){
            return;
        }

        recurringExpense.deactivate();

        recurringExpenseRepository.save(recurringExpense);
    }
}

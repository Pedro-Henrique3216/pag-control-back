package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.RecurringExpenseNotFoundException;
import com.pedrohenrique.pagcontrolback.model.RecurringExpense;
import com.pedrohenrique.pagcontrolback.repositories.RecurringExpenseRepository;

import java.util.UUID;

public class GetRecurringExpenseByIdUseCase {

    private final RecurringExpenseRepository recurringExpenseRepository;

    public GetRecurringExpenseByIdUseCase(
            RecurringExpenseRepository recurringExpenseRepository
    ) {
        this.recurringExpenseRepository = recurringExpenseRepository;
    }

    public RecurringExpense findRecurringExpenseById(UUID recurringExpenseId, UUID userId){
        if (recurringExpenseId == null) {
            throw new IllegalArgumentException("recurring expense id cannot be null");
        }

        if (userId == null) {
            throw new IllegalArgumentException("user id cannot be null");
        }

        return recurringExpenseRepository.findRecurringExpenseByIdAndUserId(recurringExpenseId, userId)
                .orElseThrow(() -> new RecurringExpenseNotFoundException("recurring expense not found"));
    }
}

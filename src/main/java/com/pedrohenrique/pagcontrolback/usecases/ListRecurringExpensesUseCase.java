package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.model.RecurringExpense;
import com.pedrohenrique.pagcontrolback.repositories.RecurringExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListRecurringExpensesUseCase {

    private final RecurringExpenseRepository recurringExpenseRepository;

    public ListRecurringExpensesUseCase(
            RecurringExpenseRepository recurringExpenseRepository
    ) {
        this.recurringExpenseRepository = recurringExpenseRepository;
    }

    public List<RecurringExpense> findRecurringExpensesByUserId(UUID userId){

        if (userId == null){
            throw new IllegalArgumentException("user id cannot be null");
        }

        return recurringExpenseRepository.findAllRecurringExpensesByUserId(userId);
    }
}

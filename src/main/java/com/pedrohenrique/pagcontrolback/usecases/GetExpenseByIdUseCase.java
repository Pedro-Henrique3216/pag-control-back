package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.ExpenseNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetExpenseByIdUseCase {

    private final ExpenseRepository expenseRepository;

    public GetExpenseByIdUseCase(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public Expense getExpenseById(UUID expenseId, UUID authenticatedUserId) {

        if (expenseId == null) {
            throw new IllegalArgumentException("Expense ID cannot be null");
        }

        if (authenticatedUserId == null) {
            throw new IllegalArgumentException("Authenticated user ID cannot be null");
        }

        return expenseRepository.findActiveExpenseByIdAndUserId(expenseId, authenticatedUserId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"));
    }
}

package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.ExpenseNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteExpenseUseCase {

    private final ExpenseRepository expenseRepository;

    public DeleteExpenseUseCase(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public void deleteExpense(UUID expenseId, UUID userId) {

        if (expenseId == null) {
            throw new IllegalArgumentException("Expense id cannot be null");
        }

        if (userId == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }

        Expense expense = expenseRepository
                .findActiveExpenseByIdAndUserId(expenseId, userId)
                .orElseThrow(() ->
                        new ExpenseNotFoundException("Expense not found")
                );

        expense.deactivate();

        expenseRepository.save(expense);
    }
}
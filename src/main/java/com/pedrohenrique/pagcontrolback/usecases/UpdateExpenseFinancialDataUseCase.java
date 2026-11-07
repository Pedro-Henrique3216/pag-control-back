package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.command.UpdateExpenseFinancialDataCommand;
import com.pedrohenrique.pagcontrolback.exceptions.ExpenseNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateExpenseFinancialDataUseCase {

    private final ExpenseRepository expenseRepository;

    public UpdateExpenseFinancialDataUseCase(
            ExpenseRepository expenseRepository
    ) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public Expense updateFinancialData(
            UUID expenseId,
            UUID userId,
            UpdateExpenseFinancialDataCommand command
    ) {
        if (expenseId == null) {
            throw new IllegalArgumentException("Expense id cannot be null");
        }

        if (userId == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }

        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }

        Expense expense = expenseRepository
                .findByExpenseIdAndUserId(expenseId, userId)
                .orElseThrow(() ->
                        new ExpenseNotFoundException("Expense not found")
                );

        Money totalAmount = command.totalAmount() != null
                ? Money.of(command.totalAmount())
                : null;

        expense.updateFinancialData(
                command.expenseDate(),
                totalAmount
        );

        return expenseRepository.save(expense);
    }
}
package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListCreditCardExpensesUseCase {

    private final ExpenseRepository expenseRepository;

    public ListCreditCardExpensesUseCase(
       ExpenseRepository expenseRepository
    ) {
        this.expenseRepository = expenseRepository;
    }

    public List<Expense> listExpenseByCreditCardId(UUID creditCardId, UUID userId) {

        if (creditCardId == null) {
            throw new IllegalArgumentException("credit card id cannot be null");
        }

        if (userId == null) {
            throw new IllegalArgumentException("user id cannot be null");
        }

        return expenseRepository.findExpensesByCreditCardIdAndUserId(creditCardId, userId);
    }
}

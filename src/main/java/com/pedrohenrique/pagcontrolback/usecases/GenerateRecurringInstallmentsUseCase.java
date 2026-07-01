package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenerateRecurringInstallmentsUseCase {

    private final ExpenseRepository expenseRepository;

    public GenerateRecurringInstallmentsUseCase(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public void execute(){
        List<Expense> expenseList = expenseRepository.findAllActiveRecurringExpenses();

        for (Expense expense : expenseList){
            expense.generateNextInstallment();
            expenseRepository.save(expense);
        }
    }
}

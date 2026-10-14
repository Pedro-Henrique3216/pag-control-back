package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.model.RecurringExpense;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import com.pedrohenrique.pagcontrolback.repositories.RecurringExpenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class GenerateRecurringExpenseUseCase {

    private final ExpenseRepository expenseRepository;
    private final RecurringExpenseRepository recurringExpenseRepository;

    public GenerateRecurringExpenseUseCase(ExpenseRepository expenseRepository, RecurringExpenseRepository recurringExpenseRepository) {
        this.expenseRepository = expenseRepository;
        this.recurringExpenseRepository = recurringExpenseRepository;
    }

    @Transactional
    public void execute(){
        LocalDate today = LocalDate.now();
        List<RecurringExpense> recurringExpensesActive = recurringExpenseRepository.findActiveRecurringExpensesDueOnOrBefore(today);
        for (RecurringExpense recurringExpense : recurringExpensesActive) {
            while (recurringExpense.isDue(today)) {
                LocalDate occurrenceDate =
                        recurringExpense.getNextOccurrence();

                boolean alreadyGenerated =
                        expenseRepository
                                .existsByRecurringExpenseIdAndOccurrenceDate(
                                        recurringExpense.getId(),
                                        occurrenceDate
                                );

                if(!alreadyGenerated){
                    Expense expense = new Expense(
                            recurringExpense.getDescription(),
                            recurringExpense.getPaymentType(),
                            recurringExpense.getStartDate(),
                            recurringExpense.getUser(),
                            recurringExpense.getAmount(),
                            recurringExpense.getCreditCard()
                    );
                    expenseRepository.save(expense);
                }

                recurringExpense.advanceNextOccurrence();
            }

            recurringExpenseRepository.save(recurringExpense);
        }
    }
}

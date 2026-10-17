package com.pedrohenrique.pagcontrolback.schedulers;

import com.pedrohenrique.pagcontrolback.usecases.GenerateRecurringExpenseUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecurringExpenseScheduler {

    private final GenerateRecurringExpenseUseCase generateRecurringExpenseUseCase;

    public RecurringExpenseScheduler(GenerateRecurringExpenseUseCase generateRecurringExpenseUseCase) {
        this.generateRecurringExpenseUseCase = generateRecurringExpenseUseCase;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void execute() {
        generateRecurringExpenseUseCase.execute();
    }
}

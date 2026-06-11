package com.pedrohenrique.pagcontrolback.schedulers;

import com.pedrohenrique.pagcontrolback.usecases.GenerateRecurringInstallmentsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecurringExpenseScheduler {

    private final GenerateRecurringInstallmentsUseCase generateRecurringInstallmentsUseCase;

    public RecurringExpenseScheduler(GenerateRecurringInstallmentsUseCase generateRecurringInstallments) {
        this.generateRecurringInstallmentsUseCase = generateRecurringInstallments;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void execute() {
        generateRecurringInstallmentsUseCase.execute();
    }
}

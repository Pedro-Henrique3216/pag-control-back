package com.pedrohenrique.pagcontrolback.schedulers;

import com.pedrohenrique.pagcontrolback.usecases.SendInstallmentReminderUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SendInstallmentReminderScheduler {

    private final SendInstallmentReminderUseCase sendInstallmentReminderUseCase;

    public SendInstallmentReminderScheduler(SendInstallmentReminderUseCase sendInstallmentReminderUseCase){
        this.sendInstallmentReminderUseCase = sendInstallmentReminderUseCase;
    }

    @Scheduled(cron = "* * 8 * * MON")
    public void execute(){
        this.sendInstallmentReminderUseCase.execute();
    }
}

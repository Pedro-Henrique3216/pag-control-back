package com.pedrohenrique.pagcontrolback.listeners;

import com.pedrohenrique.pagcontrolback.dtos.events.InstallmentReminderEvent;
import com.pedrohenrique.pagcontrolback.services.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class InstallmentReminderListener {

    private final EmailService emailService;

    public InstallmentReminderListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @EventListener
    public void execute(InstallmentReminderEvent event) throws MessagingException {
        emailService.sendInstallmentReminderEmail(
                event.email(),
                event.name(),
                event.overdue(),
                event.upcoming()
        );
    }
}

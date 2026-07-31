package com.pedrohenrique.pagcontrolback.listeners;

import com.pedrohenrique.pagcontrolback.config.security.TokenService;
import com.pedrohenrique.pagcontrolback.dtos.events.ConfirmationEmailEvent;
import com.pedrohenrique.pagcontrolback.services.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ConfirmationEmailListener {

    private final TokenService tokenService;
    private final EmailService emailService;

    public ConfirmationEmailListener(TokenService tokenService, EmailService emailService) {
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserCreated(ConfirmationEmailEvent event) throws MessagingException {
        String token = tokenService.generateTokenToEmailConfirmation(event.userId());

        emailService.sendConfirmationEmail(
                event.email(),
                event.name(),
                token
        );
    }
}

package com.pedrohenrique.pagcontrolback.listeners;

import com.pedrohenrique.pagcontrolback.dtos.events.InstallmentReminderEvent;
import com.pedrohenrique.pagcontrolback.services.EmailService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InstallmentReminderListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private InstallmentReminderListener listener;

    @Test
    void shouldSendInstallmentReminderEmail() throws MessagingException {

        InstallmentReminderEvent event = new InstallmentReminderEvent(
                "Pedro",
                "pedro@gmail.com",
                List.of(),
                List.of()
        );

        listener.execute(event);

        verify(emailService).sendInstallmentReminderEmail(
                event.email(),
                event.name(),
                event.overdue(),
                event.upcoming()
        );
    }
}
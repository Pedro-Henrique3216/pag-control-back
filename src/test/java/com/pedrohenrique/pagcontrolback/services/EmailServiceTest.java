package com.pedrohenrique.pagcontrolback.services;

import com.pedrohenrique.pagcontrolback.dtos.response.InstallmentReminderData;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;


    @Test
    void shouldSendConfirmationEmailSuccessfully() throws MessagingException {

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        when(templateEngine.process(
                eq("emails/confirmation-email"),
                any(Context.class)
        ))
                .thenReturn("<html>Email confirmation</html>");


        emailService.sendConfirmationEmail(
                "teste@gmail.com",
                "Pedro",
                "token123"
        );


        verify(templateEngine)
                .process(
                        eq("emails/confirmation-email"),
                        any(Context.class)
                );

        verify(mailSender)
                .createMimeMessage();

        verify(mailSender)
                .send(mimeMessage);
    }

    @Test
    void shouldSendInstallmentReminderEmailSuccessfully() throws MessagingException {

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        when(templateEngine.process(
                eq("emails/installment-reminder"),
                any(Context.class)
        ))
                .thenReturn("<html>Lembrete de parcelas</html>");

        List<InstallmentReminderData> overdue = List.of(
                new InstallmentReminderData(
                        "Netflix",
                        BigDecimal.valueOf(50),
                        LocalDate.now().minusDays(2),
                        "123456789"
                )
        );

        List<InstallmentReminderData> upcoming = List.of(
                new InstallmentReminderData(
                        "Internet",
                        BigDecimal.valueOf(100),
                        LocalDate.now().plusDays(3),
                        "987654321"
                )
        );

        emailService.sendInstallmentReminderEmail(
                "teste@gmail.com",
                "Pedro",
                overdue,
                upcoming
        );

        verify(templateEngine)
                .process(
                        eq("emails/installment-reminder"),
                        any(Context.class)
                );

        verify(mailSender)
                .createMimeMessage();

        verify(mailSender)
                .send(mimeMessage);
    }
}
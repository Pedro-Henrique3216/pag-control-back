package com.pedrohenrique.pagcontrolback.services;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class EmailServiceRetryTest {

    @Autowired
    private EmailService emailService;

    @MockitoBean
    private JavaMailSender mailSender;

    @MockitoBean
    private SpringTemplateEngine templateEngine;


    @Test
    void shouldRetrySendingEmailWhenMailExceptionOccurs() {

        MimeMessage message = mock(MimeMessage.class);

        when(templateEngine.process(
                eq("emails/confirmation-email"),
                any()
        ))
                .thenReturn("<html>Email</html>");


        when(mailSender.createMimeMessage())
                .thenReturn(message);


        doThrow(new MailException("SMTP error") {})
                .when(mailSender)
                .send(message);


        assertThrows(
                MailException.class,
                () -> emailService.sendConfirmationEmail(
                        "teste@gmail.com",
                        "Pedro",
                        "token123"
                )
        );


        verify(mailSender, times(3))
                .send(message);
    }
}
package com.pedrohenrique.pagcontrolback.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(
            JavaMailSender mailSender,
            SpringTemplateEngine templateEngine
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Retryable(
            retryFor = MessagingException.class,
            maxAttempts = 3,
            backoff = @Backoff(
                    delay = 2000,
                    multiplier = 2
            )
    )
    public void sendConfirmationEmail(
            String email,
            String name,
            String token
    ) throws MessagingException {

        Context context = new Context();

        context.setVariable("name", name);

        context.setVariable(
                "confirmationLink",
                "http://localhost:8080/api/users/confirm?token=" + token
        );

        String html =
                templateEngine.process(
                        "emails/confirmation-email",
                        context
                );

        MimeMessage message =
                mailSender.createMimeMessage();

        MimeMessageHelper helper =
                new MimeMessageHelper(
                        message,
                        true,
                        "UTF-8"
                );

        helper.setTo(email);
        helper.setSubject("Confirme seu email");
        helper.setText(html, true);

        mailSender.send(message);
    }
}

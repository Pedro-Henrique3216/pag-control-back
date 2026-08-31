package com.pedrohenrique.pagcontrolback.services;

import com.pedrohenrique.pagcontrolback.dtos.response.InstallmentReminderData;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;


@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    @Value("${email.confirmation-url}")
    private String confirmationUrl;

    public EmailService(
            JavaMailSender mailSender,
            SpringTemplateEngine templateEngine
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Retryable(
            retryFor = MailException.class,
            maxAttemptsExpression = "${email.retry.max-attempts}",
            backoff = @Backoff(
                    delayExpression = "${email.retry.delay}",
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
                confirmationUrl + token
        );

        String html =
                templateEngine.process(
                        "emails/confirmation-email",
                        context
                );

        sendHtmlEmail(
                email,
                "Confirme seu email",
                html
        );
    }

    @Retryable(
            retryFor = MailException.class,
            maxAttemptsExpression = "${email.retry.max-attempts}",
            backoff = @Backoff(
                    delayExpression = "${email.retry.delay}",
                    multiplier = 2
            )
    )
    public void sendInstallmentReminderEmail(
            String email,
            String name,
            List<InstallmentReminderData> overdue,
            List<InstallmentReminderData> upcoming
    ) throws MessagingException {

        Context context = new Context();

        context.setVariable("name", name);
        context.setVariable("overdue", overdue);
        context.setVariable("upcoming", upcoming);

        String html = templateEngine.process(
                "emails/installment-reminder",
                context
        );

        sendHtmlEmail(
                email,
                "Lembrete de Parcelas",
                html
        );
    }

    private void sendHtmlEmail(
            String email,
            String subject,
            String html
    ) throws MessagingException {

        MimeMessage message =
                mailSender.createMimeMessage();

        MimeMessageHelper helper =
                new MimeMessageHelper(
                        message,
                        true,
                        "UTF-8"
                );

        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(html, true);

        mailSender.send(message);
    }
}

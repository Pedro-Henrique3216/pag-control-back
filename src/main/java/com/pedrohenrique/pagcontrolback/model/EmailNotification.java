package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.ValueObjects.Email;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_notifications")
public class EmailNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "recipient_email", nullable = false)
    )
    private Email recipient;

    @Column(name = "subject_email", nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    private EmailStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "error_message")
    private String errorMessage;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    protected EmailNotification() {
    }

    public EmailNotification(
            Email recipient,
            String subject,
            String body,
            User user
    ) {
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsSent() {
        this.processedAt = LocalDateTime.now();
        this.status = EmailStatus.SENT;
    }

    public void markAsFailed(String errorMessage) {
        this.processedAt = LocalDateTime.now();
        this.status = EmailStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.exceptions.*;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@Entity
@Table(name = "recurring_expenses")
public class RecurringExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String description;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "amount")
    )
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type")
    private RecurrenceType recurrenceType;

    @Column(name = "recurrence_interval")
    private Integer recurrenceInterval;

    @Column(name = "recurrence_day")
    private Integer recurrenceDay;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "next_occurrence")
    private LocalDate nextOccurrence;

    @Column(name = "recurrence_end_date")
    private LocalDate recurrenceEndDate;

    private boolean active = true;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "credit_card_id")
    private CreditCard creditCard;

    public RecurringExpense(
            String description,
            Money amount,
            PaymentType paymentType,
            RecurrenceType recurrenceType,
            Integer recurrenceInterval,
            Integer recurrenceDay,
            LocalDate startDate,
            LocalDate recurrenceEndDate,
            User user,
            Supplier supplier,
            Category category,
            CreditCard creditCard
    ) {
        validateRecurringExpense(
                description,
                amount,
                paymentType,
                recurrenceType,
                recurrenceInterval,
                recurrenceDay,
                startDate,
                recurrenceEndDate,
                creditCard
        );
        this.description = description;
        this.amount = amount;
        this.paymentType = paymentType;
        this.recurrenceType = recurrenceType;
        this.recurrenceInterval = recurrenceInterval;
        this.recurrenceDay = recurrenceDay;
        this.startDate = startDate;
        this.recurrenceEndDate = recurrenceEndDate;
        this.user = user;
        this.supplier = supplier;
        this.category = category;
        this.creditCard = creditCard;

        this.nextOccurrence = calculateFirstOccurrence();
    }

    private void validateRecurringExpense(
            String description,
            Money amount,
            PaymentType paymentType,
            RecurrenceType recurrenceType,
            Integer recurrenceInterval,
            Integer recurrenceDay,
            LocalDate startDate,
            LocalDate recurrenceEndDate,
            CreditCard creditCard
    ) {
        validateDescription(description);
        validatePaymentType(paymentType);
        validateRecurrenceData(recurrenceType, recurrenceInterval, recurrenceDay, startDate, recurrenceEndDate);
        validateCreditCard(paymentType, creditCard, amount);
    }

    private void validateRecurrenceData(
            RecurrenceType recurrenceType,
            Integer recurrenceInterval,
            Integer recurrenceDay,
            LocalDate startDate,
            LocalDate recurrenceEndDate
    ) {
        if (recurrenceType == null) {
            throw new RecurrenceTypeRequiredException(
                    "Recurrence type is required for recurring expenses."
            );
        }

        if (recurrenceInterval == null || recurrenceInterval <= 0) {
            throw new RecurrenceIntervalException(
                    "Recurrence interval must be greater than zero."
            );
        }

        if (startDate == null) {
            throw new IllegalArgumentException(
                    "Start date is required."
            );
        }

        if (recurrenceType == RecurrenceType.MONTHLY
                && (recurrenceDay == null
                || recurrenceDay < 1
                || recurrenceDay > 31)) {
            throw new IllegalArgumentException(
                    "Recurrence day must be between 1 and 31."
            );
        }

        if (recurrenceEndDate != null
                && recurrenceEndDate.isBefore(startDate)) {
            throw new InvalidRecurrenceEndDateException(
                    "Recurrence end date cannot be before start date."
            );
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new DescriptionRequiredException("Description is required.");
        }
    }

    private void validatePaymentType(PaymentType paymentType) {
        if (paymentType == null) {
            throw new PaymentTypeRequiredException("Payment type is required.");
        }
    }

    private void validateCreditCard(PaymentType paymentType, CreditCard creditCard, Money amount) {
        if(paymentType == PaymentType.CREDIT){
            if(creditCard == null){
                throw new CreditCardRequiredException("Credit Card Required");
            }
            this.creditCard = creditCard;
            this.creditCard.hasLimit(amount);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Money getAmount() {
        return amount;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }

    public Integer getRecurrenceInterval() {
        return recurrenceInterval;
    }

    public LocalDate getNextOccurrence() {
        return nextOccurrence;
    }

    public LocalDate getRecurrenceEndDate() {
        return recurrenceEndDate;
    }

    public boolean isActive() {
        return active;
    }

    public User getUser() {
        return user;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public Category getCategory() {
        return category;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    private LocalDate calculateFirstOccurrence() {
        if (recurrenceType != RecurrenceType.MONTHLY) {
            return startDate;
        }

        YearMonth month = YearMonth.from(startDate);

        LocalDate occurrence = month.atDay(
                Math.min(recurrenceDay, month.lengthOfMonth())
        );

        if (occurrence.isBefore(startDate)) {
            month = month.plusMonths(recurrenceInterval);

            occurrence = month.atDay(
                    Math.min(recurrenceDay, month.lengthOfMonth())
            );
        }

        return occurrence;
    }

    public LocalDate calculateNextOccurrence() {
        return switch (recurrenceType) {
            case DAILY ->
                    nextOccurrence.plusDays(recurrenceInterval);

            case WEEKLY ->
                    nextOccurrence.plusWeeks(recurrenceInterval);

            case MONTHLY -> {
                YearMonth month = YearMonth.from(nextOccurrence)
                        .plusMonths(recurrenceInterval);

                yield month.atDay(
                        Math.min(recurrenceDay, month.lengthOfMonth())
                );
            }

            case YEARLY ->
                    nextOccurrence.plusYears(recurrenceInterval);
        };
    }

    public void advanceNextOccurrence() {
        LocalDate next = calculateNextOccurrence();

        if (recurrenceEndDate != null
                && next.isAfter(recurrenceEndDate)) {
            deactivate();
            return;
        }

        this.nextOccurrence = next;
    }

    public boolean isDue(LocalDate today) {
        return active
                && !nextOccurrence.isAfter(today)
                && (recurrenceEndDate == null
                || !nextOccurrence.isAfter(recurrenceEndDate));
    }

    public void deactivate() {
        this.active = false;
    }
}

package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.exceptions.*;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "credit_card")
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    @Column(name = "closing_day")
    private int closingDay;
    @Column(name = "due_day")
    private int dueDay;
    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "credit_limit")
    )
    private Money creditLimit;
    private boolean active;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public CreditCard() {}

    public CreditCard(String name, int closingDay, int dueDay, Money creditLimit, User user) {
        validateData(
                name,
                closingDay,
                dueDay,
                creditLimit
        );
        this.name = name;
        this.closingDay = closingDay;
        this.dueDay = dueDay;
        this.creditLimit = creditLimit;
        this.user = user;
        active = true;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidCreditCardNameException("Credit card name cannot be null or empty");
        }
    }

    private void validateClosingDay(int closingDay) {
        if (closingDay < 1 || closingDay > 31) {
            throw new InvalidCreditCardClosingDayException("Closing day must be between 1 and 31");
        }
    }

    private void validateDueDay(int dueDay) {
        if (dueDay < 1 || dueDay > 31) {
            throw new InvalidCreditCardDueDayException("Due day must be between 1 and 31");
        }
    }

    private void validateCreditLimit(Money creditLimit) {
        if (creditLimit == null) {
            throw new InvalidCreditLimitException("Credit limit cannot be null");
        }
    }

    private void validateData(
            String name, int closingDay, int dueDay, Money creditLimit
    ) {
        validateName(name);
        validateClosingDay(closingDay);
        validateDueDay(dueDay);
        validateCreditLimit(creditLimit);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getClosingDay() {
        return closingDay;
    }

    public int getDueDay() {
        return dueDay;
    }

    public Money getCreditLimit() {
        return creditLimit;
    }

    public boolean isActive() {
        return active;
    }

    public User getUser() {
        return user;
    }

    public void hasLimit(Money expenseValue) {
        if(expenseValue.value().compareTo(creditLimit.value()) > 0){
            throw new CreditLimitExceededException("credit limit exceeded");
        }
    }
}

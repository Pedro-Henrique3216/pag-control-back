package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.exceptions.CreditLimitExceededException;
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
        this.name = name;
        this.closingDay = closingDay;
        this.dueDay = dueDay;
        this.creditLimit = creditLimit;
        this.user = user;
        active = true;
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

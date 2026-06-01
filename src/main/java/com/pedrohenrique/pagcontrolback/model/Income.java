package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryTypeInvalidException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "incomes")
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    )
    private Money amount;
    @Column(length = 100, nullable = false)
    private String description;
    private LocalDate date;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Income() {}

    public Income(Money amount, String description, LocalDate date, User user) {
        this.amount = amount;
        this.description = description;
        this.date = date;
        setUser(user);
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Money getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDate() {
        return date;
    }

    public User getUser() {
        return user;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUser(User user) {
        try {
            this.user = Objects.requireNonNull(user);
        } catch (NullPointerException e) {
            throw new UserRequiredException("User cannot be null");
        }
    }

    public void assignCategory(Category category) {
        if(category.getCategoryType() != TransactionType.INCOME){
            throw new CategoryTypeInvalidException("Category type must be INCOME");
        }
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Income income = (Income) o;
        return Objects.equals(id, income.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

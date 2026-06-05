package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.exceptions.CategoryNameInvalidException;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryTypeInvalidException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "user_id"})
        }
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(length = 100, nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false)
    private TransactionType categoryType;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @OneToMany(mappedBy = "category")
    private Set<Expense> expenses = new HashSet<>();
    @OneToMany(mappedBy = "category")
    private Set<Income> incomes = new HashSet<>();
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    private Boolean active;

    public Category() {}

    public Category(String name, TransactionType categoryType, User user) {
        validateName(name);
        this.name = name.toLowerCase().trim();
        validateCategoryType(categoryType);
        this.categoryType = categoryType;
        setUser(user);
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new CategoryNameInvalidException("Category name cannot be null or empty");
        }
    }

    private void validateCategoryType(TransactionType categoryType) {
        if (categoryType == null) {
            throw new CategoryTypeInvalidException("Category type cannot be null");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getUser() {
        return user;
    }

    public TransactionType getCategoryType() {
        return categoryType;
    }

    public Set<Expense> getExpenses() {
        return expenses;
    }

    public Set<Income> getIncomes() {
        return incomes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public Boolean getActive() {
        return active;
    }

    public void setUser(User user) {
        try {
            this.user = Objects.requireNonNull(user);
        } catch (NullPointerException e) {
            throw new UserRequiredException("User cannot be null");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

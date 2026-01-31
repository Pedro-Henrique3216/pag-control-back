package com.pedrohenrique.pagcontrolback.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Table(name = "expenses")
@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(length = 100)
    private String invoiceNumber;
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    @Column(nullable = false)
    private LocalDate createdAt;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Installment> installments = new ArrayList<>();

    public Expense() {}

    public Expense(String invoiceNumber, Supplier supplier, User user) {
        this.invoiceNumber = invoiceNumber;
        this.supplier = supplier;
        this.createdAt = LocalDate.now();
        this.user = user;
    }

    public UUID getId() {
        return id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public List<Installment> getInstallments() {
        return installments;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return Objects.equals(id, expense.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

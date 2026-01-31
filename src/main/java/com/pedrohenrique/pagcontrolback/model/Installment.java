package com.pedrohenrique.pagcontrolback.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table(name = "installments")
@Entity
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID installmentId;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false)
    private LocalDate dueDate;
    @Column(length = 60)
    private String barcode;
    private LocalDateTime paymentDate;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InstallmentStatus status;
    @ManyToOne(optional = false)
    @JoinColumn(name = "expense_id")
    private Expense expense;

    public Installment() {
    }

    public Installment(BigDecimal amount, LocalDate dueDate, String barcode, Expense expense) {
        this.amount = amount;
        this.dueDate = dueDate;
        this.barcode = barcode;
        this.status = InstallmentStatus.UNPAID;
        this.expense = expense;
    }

    public UUID getInstallmentId() {
        return installmentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getBarcode() {
        return barcode;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public InstallmentStatus getStatus() {
        return status;
    }

    public Expense getExpense() {
        return expense;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Installment that = (Installment) o;
        return Objects.equals(installmentId, that.installmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(installmentId);
    }
}

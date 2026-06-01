package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.exceptions.*;
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
    @Column(name = "installment_id")
    private UUID installmentId;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, name = "due_date")
    private LocalDate dueDate;
    @Column(length = 60)
    private String barcode;
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InstallmentStatus status;
    @ManyToOne(optional = false)
    @JoinColumn(name = "expense_id")
    private Expense expense;
    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;
    @Column(name = "total_installments", nullable = false)
    private Integer totalInstallments;

    public Installment() {
    }

    public Installment(BigDecimal amount, LocalDate dueDate, String barcode, Expense expense, Integer installmentNumber, Integer totalInstallments) {
        validateInstallment(amount, dueDate, installmentNumber, totalInstallments);
        this.amount = amount;
        this.dueDate = dueDate;
        this.barcode = barcode;
        this.status = InstallmentStatus.UNPAID;
        this.expense = expense;
        this.installmentNumber = installmentNumber;
        this.totalInstallments = totalInstallments;
    }

    private void validateInstallment(
            BigDecimal amount,
            LocalDate dueDate,
            Integer installmentNumber,
            Integer totalInstallments
    ) {
        validateAmount(amount);

        if (dueDate == null) {
            throw new InstallmentDueDateRequiredException(
                    "Installment due date is required."
            );
        }

        if (installmentNumber == null) {
            throw new InvalidInstallmentNumberException(
                    "Installment number is required."
            );
        }

        if (totalInstallments == null) {
            throw new InvalidTotalInstallmentsException(
                    "Total installments is required."
            );
        }

        if (installmentNumber <= 0) {
            throw new InvalidInstallmentNumberException(
                    "Installment number must be greater than zero."
            );
        }

        if (totalInstallments <= 0) {
            throw new InvalidTotalInstallmentsException(
                    "Total installments must be greater than zero."
            );
        }

        if (installmentNumber > totalInstallments) {
            throw new InvalidInstallmentNumberException(
                    "Installment number cannot be greater than total installments."
            );
        }
    }

    private void validateAmount(BigDecimal amount){
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInstallmentAmountException("Installment amount must be greater than zero.");
        }
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

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public Integer getTotalInstallments() {
        return totalInstallments;
    }

    public void markAsPaid() {
        if(this.status == InstallmentStatus.PAID) {
            throw new InstallmentAlreadyPaidException("Installment is already marked as paid.");
        }
        this.paymentDate = LocalDateTime.now();
        this.status = InstallmentStatus.PAID;
    }

    public void updateInstallment(BigDecimal amount, LocalDate dueDate, String barcode) {
        if(this.status == InstallmentStatus.PAID) {
            throw new InstallmentAlreadyPaidException("Cannot update a paid installment.");
        }
        if(amount != null) {
            validateAmount(amount);
            this.amount = amount;
        }

        if(dueDate != null) {
            this.dueDate = dueDate;
        }

        if(barcode != null) {
            this.barcode = barcode;
        }
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

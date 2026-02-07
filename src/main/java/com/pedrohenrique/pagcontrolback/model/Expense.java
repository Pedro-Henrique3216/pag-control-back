package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.exceptions.*;
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
    @Column(length = 100, name = "invoice_number")
    private String invoiceNumber;
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    @Column(nullable = false, name = "created_at")
    private LocalDate createdAt;
    @Column(nullable = false, name = "expense_date")
    private LocalDate expenseDate;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Installment> installments = new ArrayList<>();

    public Expense() {}

    public Expense(String invoiceNumber, PaymentType paymentType, LocalDate expenseDate) {
        validateExpanseDate(expenseDate);
        this.invoiceNumber = invoiceNumber;
        this.createdAt = LocalDate.now();
        this.paymentType = paymentType;
        this.expenseDate = expenseDate;
    }

    private void validateExpanseDate(LocalDate expenseDate) {
        if (expenseDate == null) {
            throw new ExpenseDateRequiredException("Expense date is required.");
        }

        if (expenseDate.isAfter(LocalDate.now())) {
            throw new ExpenseDateInTheFutureException("Expense date cannot be in the future.");
        }
    }

    private void validateInstallments(PaymentType paymentType, Installment installment) {

        if (installment == null) {
            throw new InstallmentRequiredException("Installment cannot be null.");
        }

        if (paymentType == PaymentType.DEBIT || paymentType == PaymentType.CASH || paymentType == PaymentType.PIX) {
            if (!this.installments.isEmpty()) {
                throw new MultipleInstallmentsNotAllowedForPaymentTypeException("Payment type " + paymentType + " allows only one installment");
            }

            if (!installment.getDueDate().equals(this.expenseDate)) {
                throw new InvalidInstallmentDueDateForPaymentTypeException("For payment type "+ paymentType +
                        ", the installment due date must be the same as the expense date");
            }

        }
        if (installment.getDueDate().isBefore(this.expenseDate)) {
            throw new InstallmentDueDateBeforeExpenseDateException("Installment due date cannot be before the expense date.");
        }

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

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public User getUser() {
        return user;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public List<Installment> getInstallments() {
        return installments;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public void addInstallment(Installment installment) {
        validateInstallments(paymentType, installment);
        installments.add(installment);
        installment.setExpense(this);
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

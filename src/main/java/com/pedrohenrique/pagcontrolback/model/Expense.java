package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.exceptions.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Table(name = "expenses")
@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(length = 100, name = "invoice_number")
    private String invoiceNumber;
    @Column(name = "payment_type", nullable = false)
    @Enumerated(EnumType.STRING)
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
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public Expense() {}

    public Expense(String invoiceNumber, PaymentType paymentType, LocalDate expenseDate, User user, Supplier supplier) {
        validateExpanseDate(expenseDate);
        validatePaymentType(paymentType);
        this.invoiceNumber = invoiceNumber;
        this.createdAt = LocalDate.now();
        this.paymentType = paymentType;
        this.expenseDate = expenseDate;
        setUser(user);
        this.supplier = supplier;
    }

    private void validateExpanseDate(LocalDate expenseDate) {
        if (expenseDate == null) {
            throw new ExpenseDateRequiredException("Expense date is required.");
        }

        if (expenseDate.isAfter(LocalDate.now())) {
            throw new ExpenseDateInTheFutureException("Expense date cannot be in the future.");
        }
    }

    private void validatePaymentType(PaymentType paymentType) {
        if (paymentType == null) {
            throw new PaymentTypeRequiredException("Payment type is required.");
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

    public Category getCategory() {
        return category;
    }

    public void setUser(User user) {
        try {
            this.user = Objects.requireNonNull(user);
        } catch (NullPointerException e) {
            throw new UserRequiredException("User cannot be null");
        }
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public void assignCategory(Category category) {
        if(category.getCategoryType() != CategoryType.EXPENSE) {
            throw new CategoryTypeInvalidException("Category must be EXPENSE");
        }
        this.category = category;
    }

    public void addInstallment(Installment installment) {
        validateInstallments(paymentType, installment);
        installments.add(installment);
        installment.setExpense(this);
    }

    public void generateInstallments(
            BigDecimal total,
            Map<Integer, String> barcodeByDueInDays
    ) {
        if (paymentType == PaymentType.CREDIT || paymentType == PaymentType.BILL) {
            generateMultipleInstallments(total, barcodeByDueInDays);
        } else {
            generateSingleInstallment(total, barcodeByDueInDays);
        }
    }

    private void generateMultipleInstallments(
            BigDecimal total,
            Map<Integer, String> barcodeByDueInDays
    ) {
        if (barcodeByDueInDays == null || barcodeByDueInDays.isEmpty()) {
            throw new InstallmentsRequiredForPaymentTypeException("Installment intervals must be provided for CREDIT or BILL payment types.");
        }

        int count = barcodeByDueInDays.size();

        BigDecimal baseAmount = total.divide(BigDecimal.valueOf(count), 2, RoundingMode.DOWN);
        BigDecimal remainder = total.subtract(baseAmount.multiply(BigDecimal.valueOf(count)));

        int index = 0;

        for (var entry : barcodeByDueInDays.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {

            int dueInDays = entry.getKey();

            if (dueInDays <= 0) {
                throw new InvalidInstallmentDueInDaysException("Installment due in days must be greater than zero.");
            }

            index++;

            BigDecimal value = baseAmount;

            if (index == count) {
                value = value.add(remainder);
            }

            Installment installment = new Installment(
                    value,
                    expenseDate.plusDays(dueInDays),
                    entry.getValue()
            );

            this.addInstallment(installment);
        }
    }

    private void generateSingleInstallment(
            BigDecimal total,
            Map<Integer, String> barcodeByDueInDays
    ) {
        if (barcodeByDueInDays != null && barcodeByDueInDays.size() > 1) {
            throw new MultipleInstallmentsNotAllowedForPaymentTypeException(
                    "Only one installment is allowed for payment type " + this.getPaymentType()
            );
        }

        if (barcodeByDueInDays != null &&
                barcodeByDueInDays.keySet().stream().anyMatch(days -> days != 0)) {
            throw new InvalidInstallmentDueInDaysException(
                    "For payment type " + this.getPaymentType() + ", installment due in days must be 0."
            );
        }

        String barcode = barcodeByDueInDays == null
                ? null
                : barcodeByDueInDays.values().stream()
                  .filter(v -> v != null && !v.isBlank())
                  .findFirst()
                  .orElse(null);

        Installment installment = new Installment(total, expenseDate, barcode);

        this.addInstallment(installment);
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

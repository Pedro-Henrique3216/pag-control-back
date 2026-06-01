package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.exceptions.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Table(name = "expenses")
@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(length = 100, name = "invoice_number")
    private String invoiceNumber;
    @Column(length = 100, nullable = false)
    private String description;
    @Column(name = "payment_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
    @Column(nullable = false, name = "expense_date")
    private LocalDate expenseDate;
    @Column(name = "total_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Installment> installments = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Expense() {}

    public Expense(String invoiceNumber, String description, PaymentType paymentType, LocalDate expenseDate, User user, BigDecimal totalAmount) {
        validateExpenseDate(expenseDate);
        validatePaymentType(paymentType);
        validateDescription(description);
        validateTotalAmount(totalAmount);
        this.invoiceNumber = invoiceNumber;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.paymentType = paymentType;
        this.expenseDate = expenseDate;
        this.totalAmount = totalAmount;
        setUser(user);
    }

    private void validateExpenseDate(LocalDate expenseDate) {
        if (expenseDate == null) {
            throw new ExpenseDateRequiredException("Expense date is required.");
        }

        if (expenseDate.isAfter(LocalDate.now())) {
            throw new ExpenseDateInTheFutureException("Expense date cannot be in the future.");
        }
    }

    private void validateTotalAmount(BigDecimal totalAmount) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Total amount must be greater than zero.");
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

    private void validateInstallments(Installment installment) {

        if (installment == null) {
            throw new InstallmentRequiredException("Installment cannot be null.");
        }

        if (this.paymentType == PaymentType.DEBIT || this.paymentType == PaymentType.CASH || this.paymentType == PaymentType.PIX) {
            if (!this.installments.isEmpty()) {
                throw new MultipleInstallmentsNotAllowedForPaymentTypeException("Payment type " + this.paymentType + " allows only one installment");
            }

            if (!installment.getDueDate().equals(this.expenseDate)) {
                throw new InvalidInstallmentDueDateForPaymentTypeException("For payment type "+ this.paymentType +
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

    public LocalDateTime getCreatedAt() {
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

    public String getDescription() {
        return description;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUser(User user) {
        if (user == null) {
            throw new UserRequiredException("User cannot be null");
        }
        this.user = user;
    }

    public void setSupplier(Supplier supplier) {
        if (supplier == null) {
            throw new SupplierRequiredException("Supplier cannot be null");
        }
        this.supplier = supplier;
    }

    public void assignCategory(Category category) {
        if (category == null) {
            throw new CategoryRequiredException("Category cannot be null");
        }
        if(category.getCategoryType() != TransactionType.EXPENSE) {
            throw new CategoryTypeInvalidException("Category must be EXPENSE");
        }
        this.category = category;
    }

    public void addInstallment(Installment installment) {
        validateInstallments(installment);
        installments.add(installment);
    }

    public void generateInstallments(
            Map<Integer, String> barcodeByDueInDays
    ) {

        if (!this.installments.isEmpty()) {
            throw new InstallmentsAlreadyGeneratedException(
                    "Installments have already been generated for this expense."
            );
        }

        boolean allowsMultipleInstallments =
                this.paymentType == PaymentType.CREDIT ||
                        this.paymentType == PaymentType.BILL;

        if (allowsMultipleInstallments) {
            generateMultipleInstallments(barcodeByDueInDays);
            return;
        }

        generateSingleInstallment(barcodeByDueInDays);
    }

    private void generateMultipleInstallments(
            Map<Integer, String> barcodeByDueInDays
    ) {
        if (barcodeByDueInDays == null || barcodeByDueInDays.isEmpty()) {
            throw new InstallmentsRequiredForPaymentTypeException("Installment intervals must be provided for CREDIT or BILL payment types.");
        }

        int count = barcodeByDueInDays.size();

        BigDecimal baseAmount = totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.DOWN);
        BigDecimal remainder = totalAmount.subtract(baseAmount.multiply(BigDecimal.valueOf(count)));

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
                    entry.getValue() == null || entry.getValue().isBlank() ? null : entry.getValue(),
                    this,
                    index,
                    count
            );

            this.addInstallment(installment);
        }
    }

    private void generateSingleInstallment(
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

        Installment installment = new Installment(totalAmount, expenseDate, barcode, this, 1, 1);
        installment.markAsPaid();

        this.addInstallment(installment);
    }

    public boolean isPaidOff() {
        return installments.stream().allMatch(installment -> installment.getStatus() == InstallmentStatus.PAID);
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

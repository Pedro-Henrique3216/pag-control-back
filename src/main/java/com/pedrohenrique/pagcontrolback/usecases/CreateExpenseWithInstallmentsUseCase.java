package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.*;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
public class CreateExpenseWithInstallmentsUseCase {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;

    public CreateExpenseWithInstallmentsUseCase(
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            SupplierRepository supplierRepository,
            CategoryRepository categoryRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.supplierRepository = supplierRepository;
        this.categoryRepository = categoryRepository;
    }

    public Expense execute(UUID userId, UUID supplierId, UUID categoryId, Expense expense, Map<Integer, String> barcodeByDueInDays, BigDecimal amount) {

        if (expense == null) {
            throw new ExpenseRequiredException("Expense is required");
        }

        if (expense.getPaymentType() == null) {
            throw new PaymentTypeRequiredException("Payment type is required");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidExpenseAmountException("Total amount must be greater than zero.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Supplier supplier = supplierRepository.findById(supplierId)
                        .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + supplierId));

        if(categoryId != null) {
            Category category = categoryRepository.findCategoryByIdAndUserId(categoryId, userId)
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));
            expense.assignCategory(category);
        }

        expense.setUser(user);
        expense.setSupplier(supplier);

        if(expense.getPaymentType() == PaymentType.CREDIT || expense.getPaymentType() == PaymentType.BILL) {

            if(barcodeByDueInDays == null || barcodeByDueInDays.isEmpty()) {
                throw new InstallmentsRequiredForPaymentTypeException("Installment intervals must be provided for CREDIT or BILL payment types.");
            }

            BigDecimal installmentAmount =
                    amount.divide(
                            BigDecimal.valueOf(barcodeByDueInDays.size()),
                            2,
                            RoundingMode.HALF_UP
                    );

            barcodeByDueInDays.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        Integer dueInDays = entry.getKey();
                        if (dueInDays <= 0) {
                            throw new InvalidInstallmentDueInDaysException("Installment due in days must be greater than zero.");
                        }
                        String barcode = entry.getValue();

                        Installment installment = new Installment(
                                installmentAmount,
                                calculateDueDate(expense.getExpenseDate(), dueInDays),
                                barcode
                        );
                        expense.addInstallment(installment);
                    });

        } else {
            if (barcodeByDueInDays != null && barcodeByDueInDays.size() > 1) {
                throw new MultipleInstallmentsNotAllowedForPaymentTypeException(
                        "Only one installment is allowed for payment type " + expense.getPaymentType()
                );
            }

            if (barcodeByDueInDays != null && barcodeByDueInDays.keySet().stream().anyMatch(days -> days != 0)) {
                throw new InvalidInstallmentDueInDaysException(
                        "For payment type " + expense.getPaymentType() + ", installment due in days must be 0."
                );
            }

            String barcode = null;

            if (barcodeByDueInDays != null && !barcodeByDueInDays.isEmpty()) {
                barcode = barcodeByDueInDays.values().iterator().next();
            }

            Installment installment = new Installment(amount, expense.getExpenseDate(), barcode);
            expense.addInstallment(installment);
        }

        return expenseRepository.save(expense);
    }

    private LocalDate calculateDueDate(LocalDate expenseDay, int intervalInDays) {
        return expenseDay.plusDays(intervalInDays);
    }

}
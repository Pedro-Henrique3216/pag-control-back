package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.CreateExpenseCommand;
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
import java.util.concurrent.atomic.AtomicInteger;

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

    public Expense execute(CreateExpenseCommand command) {

        if (command == null) {
            throw new CreateExpenseCommandRequiredException("Create expense command is required");
        }

        if (command.totalAmount() == null || command.totalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidExpenseAmountException("Total amount must be greater than zero.");
        }

        if (command.userId() == null) {
            throw new UserIdRequiredException("User ID is required.");
        }

        if (command.supplierId() == null) {
            throw new SupplierRequiredException("Supplier ID is required.");
        }

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + command.userId()));

        Supplier supplier = supplierRepository.findById(command.supplierId())
                        .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + command.supplierId()));

        Expense expense = new Expense(
                command.invoiceNumber(),
                command.paymentType(),
                command.date(),
                user,
                supplier
        );

        if(command.categoryId() != null) {
            Category category = categoryRepository.findCategoryByIdAndUserId(command.categoryId(), command.userId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + command.categoryId()));
            expense.assignCategory(category);
        }

        if(expense.getPaymentType() == PaymentType.CREDIT || expense.getPaymentType() == PaymentType.BILL) {

            if(command.barcodeByDueInDays() == null || command.barcodeByDueInDays().isEmpty()) {
                throw new InstallmentsRequiredForPaymentTypeException("Installment intervals must be provided for CREDIT or BILL payment types.");
            }

            BigDecimal total = command.totalAmount();
            int count = command.barcodeByDueInDays().size();

            BigDecimal baseAmount = total.divide(BigDecimal.valueOf(count), 2, RoundingMode.DOWN);
            BigDecimal remainder = total.subtract(baseAmount.multiply(BigDecimal.valueOf(count)));

            AtomicInteger index = new AtomicInteger(0);

            command.barcodeByDueInDays().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {

                        BigDecimal installmentValue = baseAmount;

                        if (index.incrementAndGet() == count) {
                            installmentValue = installmentValue.add(remainder);
                        }

                        Installment installment = new Installment(
                                installmentValue,
                                calculateDueDate(expense.getExpenseDate(), entry.getKey()),
                                entry.getValue()
                        );

                        expense.addInstallment(installment);
                    });

        } else {
            if (command.barcodeByDueInDays() != null && command.barcodeByDueInDays().size() > 1) {
                throw new MultipleInstallmentsNotAllowedForPaymentTypeException(
                        "Only one installment is allowed for payment type " + expense.getPaymentType()
                );
            }

            if (command.barcodeByDueInDays() != null && command.barcodeByDueInDays().keySet().stream().anyMatch(days -> days != 0)) {
                throw new InvalidInstallmentDueInDaysException(
                        "For payment type " + expense.getPaymentType() + ", installment due in days must be 0."
                );
            }

            String barcode = null;

            if (command.barcodeByDueInDays() != null && !command.barcodeByDueInDays().isEmpty()) {
                barcode = command.barcodeByDueInDays().values().iterator().next();
            }

            Installment installment = new Installment(command.totalAmount(), expense.getExpenseDate(), barcode);
            expense.addInstallment(installment);
        }

        return expenseRepository.save(expense);
    }

    private LocalDate calculateDueDate(LocalDate expenseDay, int intervalInDays) {
        return expenseDay.plusDays(intervalInDays);
    }

}
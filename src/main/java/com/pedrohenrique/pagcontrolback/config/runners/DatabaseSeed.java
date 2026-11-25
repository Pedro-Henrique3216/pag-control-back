
package com.pedrohenrique.pagcontrolback.config.runners;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Configuration
@Profile("dev")
public class DatabaseSeed {

    @Bean
    CommandLineRunner seedDatabase(
            UserRepository userRepository,
            SupplierRepository supplierRepository,
            CategoryRepository categoryRepository,
            CreditCardRepository creditCardRepository,
            ExpenseRepository expenseRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            User user = new User(
                    "Pedro Henrique",
                    null,
                    "pedro@mail.com",
                    passwordEncoder.encode("Teste123"),
                    "11999990001",
                    PersonType.PF
            );

            user.verifyEmail();
            userRepository.save(user);

            Supplier supplier = new Supplier(
                    "Fornecedor de exemplo",
                    null,
                    user
            );
            supplierRepository.save(supplier);

            Category expenseCategory = new Category(
                    "Despesas gerais",
                    TransactionType.EXPENSE,
                    user
            );
            categoryRepository.save(expenseCategory);

            CreditCard creditCard = new CreditCard(
                    "Cartão principal",
                    10,
                    20,
                    Money.of(BigDecimal.valueOf(5000)),
                    user
            );
            creditCardRepository.save(creditCard);

            LocalDate today = LocalDate.now();

            Expense creditExpense = new Expense(
                    "INV-001",
                    "Compra parcelada",
                    PaymentType.CREDIT,
                    today.minusDays(5),
                    user,
                    Money.of(BigDecimal.valueOf(600)),
                    creditCard
            );

            creditExpense.setSupplier(supplier);
            creditExpense.assignCategory(expenseCategory);
            creditExpense.generateCreditCardInstallments(3);

            expenseRepository.save(creditExpense);

            Expense debitExpense = new Expense(
                    "INV-002",
                    "Compra no débito",
                    PaymentType.DEBIT,
                    today.minusDays(3),
                    user,
                    Money.of(BigDecimal.valueOf(150))
            );

            debitExpense.setSupplier(supplier);
            debitExpense.assignCategory(expenseCategory);
            debitExpense.generateSingleInstallment();

            expenseRepository.save(debitExpense);

            Expense cashExpense = new Expense(
                    "INV-003",
                    "Compra em dinheiro",
                    PaymentType.CASH,
                    today.minusDays(2),
                    user,
                    Money.of(BigDecimal.valueOf(80))
            );

            cashExpense.setSupplier(supplier);
            cashExpense.assignCategory(expenseCategory);
            cashExpense.generateSingleInstallment();

            expenseRepository.save(cashExpense);

            Expense pixExpense = new Expense(
                    "INV-004",
                    "Pagamento via PIX",
                    PaymentType.PIX,
                    today.minusDays(1),
                    user,
                    Money.of(BigDecimal.valueOf(200))
            );

            pixExpense.setSupplier(supplier);
            pixExpense.assignCategory(expenseCategory);
            pixExpense.generateSingleInstallment();

            expenseRepository.save(pixExpense);

            Expense billExpense = new Expense(
                    "INV-005",
                    "Pagamento de boleto",
                    PaymentType.BILL,
                    today.minusDays(1),
                    user,
                    Money.of(BigDecimal.valueOf(400))
            );

            billExpense.setSupplier(supplier);
            billExpense.assignCategory(expenseCategory);

            billExpense.generateBillInstallments(
                    Map.of(
                            15, "BARCODE-005-1",
                            30, "BARCODE-005-2"
                    )
            );

            expenseRepository.save(billExpense);
        };
    }
}

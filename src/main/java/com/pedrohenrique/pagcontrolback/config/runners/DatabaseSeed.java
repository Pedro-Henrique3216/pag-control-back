package com.pedrohenrique.pagcontrolback.config.runners;

import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepository;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
@Profile("dev")
public class DatabaseSeed {

    @Bean
    CommandLineRunner seedDatabase(
            UserRepository userRepository,
            SupplierRepository supplierRepository,
            ExpenseRepository expenseRepository,
            InstallmentRepository installmentRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            if (userRepository.count() > 0) {
                return;
            }

            for (int u = 1; u <= 5; u++) {

                User user = new User(
                        "User " + u,
                        null,
                        "user" + u + "@mail.com",
                        passwordEncoder.encode("123456"),
                        "1199999000" + u,
                        PersonType.PF
                );

                userRepository.save(user);

                Supplier supplier = new Supplier("Supplier User " + u);
                supplier.setUser(user);
                supplierRepository.save(supplier);

                for (int e = 1; e <= 5; e++) {

                    boolean isDebitExpense = (e % 2 == 0);

                    PaymentType paymentType = isDebitExpense
                            ? PaymentType.DEBIT
                            : PaymentType.CREDIT;

                    Expense expense = new Expense(
                            "INV-" + u + "-" + e,
                            paymentType,
                            LocalDate.now().minusDays(e),
                            user,
                            supplier
                    );

                    expenseRepository.save(expense);

                    if (paymentType == PaymentType.DEBIT) {

                        Installment installment = new Installment(
                                BigDecimal.valueOf(100),
                                LocalDate.now().plusDays(10),
                                "BARCODE-" + u + "-" + e + "-1"
                        );

                        installment.setExpense(expense);
                        installmentRepository.save(installment);

                    } else {

                        int numberOfInstallments = ThreadLocalRandom.current()
                                .nextInt(1, 6);

                        BigDecimal total = BigDecimal.valueOf(500);
                        BigDecimal valuePerInstallment =
                                total.divide(BigDecimal.valueOf(numberOfInstallments), 2, RoundingMode.HALF_UP);

                        for (int i = 1; i <= numberOfInstallments; i++) {

                            Installment installment = new Installment(
                                    valuePerInstallment,
                                    LocalDate.now().plusMonths(i),
                                    "BARCODE-" + u + "-" + e + "-" + i
                            );

                            installment.setExpense(expense);
                            installmentRepository.save(installment);
                        }
                    }
                }
            }
        };
    }
}

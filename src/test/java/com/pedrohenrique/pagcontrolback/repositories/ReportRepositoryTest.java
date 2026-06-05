package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.response.MonthSummaryDto;
import com.pedrohenrique.pagcontrolback.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(ReportRepository.class)
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    private User createUser(String email) {
        return userRepository.save(
                new User(
                        "Pedro",
                        null,
                        email,
                        "$2a$10$abcdefghijklmnopqrstuv",
                        "11912345678",
                        PersonType.PF
                )
        );
    }

    private Supplier createSupplier(User user, String name) {
        return supplierRepository.save(
                new Supplier(
                        name,
                        null,
                        user
                )
        );
    }

    private Expense createExpense(
            String description,
            PaymentType paymentType,
            LocalDate date,
            User user,
            Supplier supplier,
            BigDecimal amount,
            Map<Integer, String> installments
    ) {

        Expense expense = new Expense(
                "INV-dsadasd",
                description,
                paymentType,
                date,
                user,
                new Money(amount)
        );

        expense.setSupplier(supplier);

        expense.generateInstallments(installments);

        return expenseRepository.save(expense);
    }

    private Income createIncome(
            BigDecimal amount,
            String description,
            LocalDate date,
            User user
    ) {

        Income income = new Income(
                new Money(amount),
                description,
                date,
                user
        );

        return incomeRepository.save(income);
    }

    @Nested
    class MonthlySummaryTests {

        @Test
        @DisplayName("Should return monthly summary with income and expenses")
        void shouldReturnMonthlySummary() {

            User user = createUser("summary@test.com");

            Supplier supplier = createSupplier(
                    user,
                    "Supplier A"
            );

            createIncome(
                    BigDecimal.valueOf(3000),
                    "income",
                    LocalDate.of(2026, 2, 10),
                    user
            );

            createExpense(
                    "expense",
                    PaymentType.CASH,
                    LocalDate.of(2026, 2, 15),
                    user,
                    supplier,
                    BigDecimal.valueOf(1500),
                    Map.of(0, "")
            );

            List<MonthSummaryDto> result =
                    reportRepository.findMonthlySummaryByUserId(
                            user.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertEquals(1, result.size());

            MonthSummaryDto summary = result.get(0);

            assertEquals(
                    YearMonth.of(2026, 2),
                    summary.month()
            );

            assertEquals(
                    new BigDecimal("3000").floatValue(),
                    summary.income().floatValue()
            );

            assertEquals(
                    new BigDecimal("1500").floatValue(),
                    summary.expense().floatValue()
            );
        }

        @Test
        void shouldIgnoreUnpaidInstallments() {

            User user = createUser("unpaid@test.com");

            Supplier supplier = createSupplier(
                    user,
                    "Supplier A"
            );

            createExpense(
                    "expense",
                    PaymentType.BILL,
                    LocalDate.of(2026, 2, 10),
                    user,
                    supplier,
                    BigDecimal.valueOf(2000),
                    Map.of(1, "")
            );

            List<MonthSummaryDto> result =
                    reportRepository.findMonthlySummaryByUserId(
                            user.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertEquals(0, result.size());
        }

        @Test
        void shouldReturnEmptyWhenNoData() {

            User user = createUser("empty@test.com");

            List<MonthSummaryDto> result =
                    reportRepository.findMonthlySummaryByUserId(
                            user.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldGroupByMultipleMonths() {

            User user = createUser("months@test.com");

            createIncome(
                    BigDecimal.valueOf(1000),
                    "jan",
                    LocalDate.of(2026, 1, 10),
                    user
            );

            createIncome(
                    BigDecimal.valueOf(2000),
                    "fev",
                    LocalDate.of(2026, 2, 10),
                    user
            );

            List<MonthSummaryDto> result =
                    reportRepository.findMonthlySummaryByUserId(
                            user.getId(),
                            LocalDate.of(2026, 1, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertEquals(2, result.size());

            assertEquals(
                    BigDecimal.ZERO.intValue(),
                    result.get(0).expense().intValue()
            );
        }

        @Test
        void shouldSumMultipleValuesInSameMonth() {

            User user = createUser("sum@test.com");

            Supplier supplier = createSupplier(
                    user,
                    "Supplier A"
            );

            createIncome(
                    BigDecimal.valueOf(1000),
                    "i1",
                    LocalDate.of(2026, 2, 10),
                    user
            );

            createIncome(
                    BigDecimal.valueOf(500),
                    "i2",
                    LocalDate.of(2026, 2, 15),
                    user
            );

            createExpense(
                    "e1",
                    PaymentType.CASH,
                    LocalDate.of(2026, 2, 10),
                    user,
                    supplier,
                    BigDecimal.valueOf(300),
                    Map.of(0, "")
            );

            createExpense(
                    "e2",
                    PaymentType.CASH,
                    LocalDate.of(2026, 2, 15),
                    user,
                    supplier,
                    BigDecimal.valueOf(200),
                    Map.of(0, "")
            );

            List<MonthSummaryDto> result =
                    reportRepository.findMonthlySummaryByUserId(
                            user.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            MonthSummaryDto summary = result.get(0);

            assertEquals(
                    new BigDecimal("1500").floatValue(),
                    summary.income().floatValue()
            );

            assertEquals(
                    new BigDecimal("500").floatValue(),
                    summary.expense().floatValue()
            );
        }

        @Test
        void shouldReturnOnlyDataFromRequestedUser() {

            User user1 = createUser("user1@test.com");

            User user2 = createUser("user2@test.com");

            createIncome(
                    BigDecimal.valueOf(1000),
                    "income-user1",
                    LocalDate.of(2026, 2, 10),
                    user1
            );

            createIncome(
                    BigDecimal.valueOf(5000),
                    "income-user2",
                    LocalDate.of(2026, 2, 10),
                    user2
            );

            List<MonthSummaryDto> result =
                    reportRepository.findMonthlySummaryByUserId(
                            user1.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertEquals(1, result.size());

            assertEquals(
                    new BigDecimal("1000").floatValue(),
                    result.get(0).income().floatValue()
            );
        }
    }
}
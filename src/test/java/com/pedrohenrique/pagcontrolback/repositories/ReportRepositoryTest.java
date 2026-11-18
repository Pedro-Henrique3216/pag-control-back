package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.response.MonthSummaryDto;
import com.pedrohenrique.pagcontrolback.model.*;
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
import java.util.UUID;

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
                new Supplier(name, null, user)
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
                "INV-" + UUID.randomUUID(),
                description,
                paymentType,
                date,
                user,
                new Money(amount)
        );

        expense.setSupplier(supplier);

        if (paymentType.equals(PaymentType.CREDIT)) {
            expense.generateCreditCardInstallments(1);
        } else if (paymentType.equals(PaymentType.BILL)) {
            expense.generateBillInstallments(installments);
        } else {
            expense.generateSingleInstallment();
        }

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

    private void assertAmount(
            BigDecimal expected,
            BigDecimal actual
    ) {
        assertEquals(
                0,
                expected.compareTo(actual),
                "Expected: " + expected + ", actual: " + actual
        );
    }

    @Nested
    class MonthlySummaryTests {

        @Test
        void shouldReturnMonthlySummary() {
            User user = createUser("summary@test.com");

            Supplier supplier = createSupplier(user, "Supplier A");

            createIncome(
                    BigDecimal.valueOf(3000),
                    "Salary",
                    LocalDate.of(2026, 2, 10),
                    user
            );

            createExpense(
                    "Expense",
                    PaymentType.CASH,
                    LocalDate.of(2026, 2, 15),
                    user,
                    supplier,
                    BigDecimal.valueOf(1500),
                    null
            );

            List<MonthSummaryDto> result =
                    reportRepository.findMonthlySummaryByUserId(
                            user.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertEquals(1, result.size());

            MonthSummaryDto summary = result.get(0);

            assertEquals(YearMonth.of(2026, 2), summary.month());

            assertAmount(
                    BigDecimal.valueOf(3000),
                    summary.income()
            );

            assertAmount(
                    BigDecimal.valueOf(1500),
                    summary.expense()
            );
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
                    "January",
                    LocalDate.of(2026, 1, 10),
                    user
            );

            createIncome(
                    BigDecimal.valueOf(2000),
                    "February",
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
                    YearMonth.of(2026, 1),
                    result.get(0).month()
            );

            assertEquals(
                    YearMonth.of(2026, 2),
                    result.get(1).month()
            );

            assertAmount(
                    BigDecimal.valueOf(1000),
                    result.get(0).income()
            );

            assertAmount(
                    BigDecimal.valueOf(2000),
                    result.get(1).income()
            );

            assertAmount(
                    BigDecimal.ZERO,
                    result.get(0).expense()
            );

            assertAmount(
                    BigDecimal.ZERO,
                    result.get(1).expense()
            );
        }

        @Test
        void shouldSumMultipleValuesInSameMonth() {
            User user = createUser("sum@test.com");

            Supplier supplier = createSupplier(user, "Supplier A");

            createIncome(
                    BigDecimal.valueOf(1000),
                    "Income 1",
                    LocalDate.of(2026, 2, 10),
                    user
            );

            createIncome(
                    BigDecimal.valueOf(500),
                    "Income 2",
                    LocalDate.of(2026, 2, 15),
                    user
            );

            createExpense(
                    "Expense 1",
                    PaymentType.CASH,
                    LocalDate.of(2026, 2, 10),
                    user,
                    supplier,
                    BigDecimal.valueOf(300),
                    null
            );

            createExpense(
                    "Expense 2",
                    PaymentType.CASH,
                    LocalDate.of(2026, 2, 15),
                    user,
                    supplier,
                    BigDecimal.valueOf(500),
                    null
            );

            List<MonthSummaryDto> result =
                    reportRepository.findMonthlySummaryByUserId(
                            user.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertEquals(1, result.size());

            MonthSummaryDto summary = result.get(0);

            assertAmount(
                    BigDecimal.valueOf(1500),
                    summary.income()
            );

            assertAmount(
                    BigDecimal.valueOf(800),
                    summary.expense()
            );
        }
    }

    @Nested
    class InstallmentTests {

        @Test
        void shouldIgnoreUnpaidInstallments() {
            User user = createUser("unpaid@test.com");

            Supplier supplier = createSupplier(user, "Supplier A");

            createExpense(
                    "Unpaid expense",
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

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class UserIsolationTests {

        @Test
        void shouldReturnOnlyDataFromRequestedUser() {
            User user1 = createUser("user1@test.com");
            User user2 = createUser("user2@test.com");

            createIncome(
                    BigDecimal.valueOf(1000),
                    "Income user 1",
                    LocalDate.of(2026, 2, 10),
                    user1
            );

            createIncome(
                    BigDecimal.valueOf(5000),
                    "Income user 2",
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

            assertAmount(
                    BigDecimal.valueOf(1000),
                    result.get(0).income()
            );

            assertAmount(
                    BigDecimal.ZERO,
                    result.get(0).expense()
            );
        }
    }

    @Nested
    class InactiveTransactionTests {

        @Test
        void shouldIgnoreInactiveIncomes() {
            User user = createUser("inactive-income@test.com");

            Income income = createIncome(
                    BigDecimal.valueOf(3000),
                    "Deleted income",
                    LocalDate.of(2026, 2, 10),
                    user
            );

            income.deactivate();
            incomeRepository.saveAndFlush(income);

            List<MonthSummaryDto> result =
                    reportRepository.findMonthlySummaryByUserId(
                            user.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldIgnoreInactiveExpenses() {
            User user = createUser("inactive-expense@test.com");

            Supplier supplier = createSupplier(user, "Supplier A");

            createIncome(
                    BigDecimal.valueOf(1000),
                    "Active income",
                    LocalDate.of(2026, 2, 10),
                    user
            );

            Expense expense = createExpense(
                    "Deleted expense",
                    PaymentType.CASH,
                    LocalDate.of(2026, 2, 15),
                    user,
                    supplier,
                    BigDecimal.valueOf(500),
                    null
            );

            expense.deactivate();
            expenseRepository.saveAndFlush(expense);

            List<MonthSummaryDto> result =
                    reportRepository.findMonthlySummaryByUserId(
                            user.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertEquals(1, result.size());

            MonthSummaryDto summary = result.get(0);

            assertAmount(
                    BigDecimal.valueOf(1000),
                    summary.income()
            );

            assertAmount(
                    BigDecimal.ZERO,
                    summary.expense()
            );
        }

        @Test
        void shouldIgnoreBothInactiveIncomesAndExpenses() {
            User user = createUser("inactive-both@test.com");

            Supplier supplier = createSupplier(user, "Supplier A");

            Income income = createIncome(
                    BigDecimal.valueOf(3000),
                    "Deleted income",
                    LocalDate.of(2026, 2, 10),
                    user
            );

            Expense expense = createExpense(
                    "Deleted expense",
                    PaymentType.CASH,
                    LocalDate.of(2026, 2, 15),
                    user,
                    supplier,
                    BigDecimal.valueOf(1000),
                    null
            );

            income.deactivate();
            expense.deactivate();

            incomeRepository.saveAndFlush(income);
            expenseRepository.saveAndFlush(expense);

            List<MonthSummaryDto> result =
                    reportRepository.findMonthlySummaryByUserId(
                            user.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class TransactionTypeTests {

        @Test
        void shouldReturnZeroExpensesWhenThereAreOnlyIncomes() {
            User user = createUser("only-income@test.com");

            createIncome(
                    BigDecimal.valueOf(2500),
                    "Salary",
                    LocalDate.of(2026, 2, 10),
                    user
            );

            List<MonthSummaryDto> result =
                    reportRepository.findMonthlySummaryByUserId(
                            user.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertEquals(1, result.size());

            assertAmount(
                    BigDecimal.valueOf(2500),
                    result.get(0).income()
            );

            assertAmount(
                    BigDecimal.ZERO,
                    result.get(0).expense()
            );
        }

        @Test
        void shouldReturnZeroIncomesWhenThereAreOnlyExpenses() {
            User user = createUser("only-expense@test.com");

            Supplier supplier = createSupplier(user, "Supplier A");

            createExpense(
                    "Purchase",
                    PaymentType.CASH,
                    LocalDate.of(2026, 2, 10),
                    user,
                    supplier,
                    BigDecimal.valueOf(800),
                    null
            );

            List<MonthSummaryDto> result =
                    reportRepository.findMonthlySummaryByUserId(
                            user.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertEquals(1, result.size());

            assertAmount(
                    BigDecimal.ZERO,
                    result.get(0).income()
            );

            assertAmount(
                    BigDecimal.valueOf(800),
                    result.get(0).expense()
            );
        }
    }

    @Nested
    class DateRangeTests {

        @Test
        void shouldConsiderEntireMonthsEvenWhenPartialDatesAreProvided() {
            User user = createUser("partial-date@test.com");

            createIncome(
                    BigDecimal.valueOf(100),
                    "First day",
                    LocalDate.of(2026, 2, 1),
                    user
            );

            createIncome(
                    BigDecimal.valueOf(200),
                    "Last day",
                    LocalDate.of(2026, 2, 28),
                    user
            );

            createIncome(
                    BigDecimal.valueOf(500),
                    "Previous month",
                    LocalDate.of(2026, 1, 31),
                    user
            );

            createIncome(
                    BigDecimal.valueOf(700),
                    "Next month",
                    LocalDate.of(2026, 3, 1),
                    user
            );

            List<MonthSummaryDto> result =
                    reportRepository.findMonthlySummaryByUserId(
                            user.getId(),
                            LocalDate.of(2026, 2, 15),
                            LocalDate.of(2026, 2, 16)
                    );

            assertEquals(1, result.size());

            assertEquals(
                    YearMonth.of(2026, 2),
                    result.get(0).month()
            );

            assertAmount(
                    BigDecimal.valueOf(300),
                    result.get(0).income()
            );
        }

        @Test
        void shouldNotReturnTransactionsOutsideRequestedPeriod() {
            User user = createUser("outside-period@test.com");

            createIncome(
                    BigDecimal.valueOf(1000),
                    "January",
                    LocalDate.of(2026, 1, 15),
                    user
            );

            createIncome(
                    BigDecimal.valueOf(2000),
                    "March",
                    LocalDate.of(2026, 3, 15),
                    user
            );

            List<MonthSummaryDto> result =
                    reportRepository.findMonthlySummaryByUserId(
                            user.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertTrue(result.isEmpty());
        }
    }
}

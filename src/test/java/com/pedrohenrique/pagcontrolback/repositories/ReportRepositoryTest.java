package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.dtos.response.MonthSummaryDto;
import com.pedrohenrique.pagcontrolback.model.*;
import org.junit.jupiter.api.DisplayName;
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

    @Test
    @DisplayName("Should return monthly summary with income and expenses")
    void shouldReturnMonthlySummary() {

        User user = userRepository.save(
                new User("Pedro", null, "email@test.com", "123", "11912345678", PersonType.PF)
        );

        Supplier supplier = supplierRepository.save(
                new Supplier("Supplier A", null, user)
        );

        incomeRepository.save(
                new Income(
                        BigDecimal.valueOf(3000),
                        "income",
                        LocalDate.of(2026, 2, 10),
                        user
                )
        );

        Expense expense = new Expense(
                "expense",
                PaymentType.CASH,
                LocalDate.of(2026, 2, 15),
                user,
                supplier
        );

        expense.generateInstallments(BigDecimal.valueOf(1500), Map.of(0, ""));
        expenseRepository.save(expense);

        List<MonthSummaryDto> result = reportRepository.findMonthlySummaryByUserId(
                user.getId(),
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        );

        assertEquals(1, result.size());

        MonthSummaryDto summary = result.get(0);

        assertEquals(YearMonth.of(2026, 2), summary.month());
        assertEquals(new BigDecimal("3000").floatValue(), summary.income().floatValue());
        assertEquals(new BigDecimal("1500").floatValue(), summary.expense().floatValue());
    }

    @Test
    void shouldIgnoreUnpaidInstallments() {

        User user = userRepository.save(
                new User("Pedro", null, "email@test.com", "123", "11912345678", PersonType.PF)
        );

        Supplier supplier = supplierRepository.save(
                new Supplier("Supplier A", null, user)
        );

        Expense expense = new Expense(
                "expense",
                PaymentType.BILL,
                LocalDate.of(2026, 2, 10),
                user,
                supplier
        );

        expense.generateInstallments(BigDecimal.valueOf(2000), Map.of(1, ""));

        expenseRepository.save(expense);

        List<MonthSummaryDto> result = reportRepository.findMonthlySummaryByUserId(
                user.getId(),
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        );

        assertEquals(0, result.size());
    }

    @Test
    void shouldReturnEmptyWhenNoData() {

        User user = userRepository.save(
                new User("Pedro", null, "email@test.com", "123", "11912345678", PersonType.PF)
        );

        List<MonthSummaryDto> result = reportRepository.findMonthlySummaryByUserId(
                user.getId(),
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGroupByMultipleMonths() {

        User user = userRepository.save(
                new User("Pedro", null, "email@test.com", "123", "11912345678", PersonType.PF)
        );

        incomeRepository.save(
                new Income(BigDecimal.valueOf(1000), "jan", LocalDate.of(2026, 1, 10), user)
        );

        incomeRepository.save(
                new Income(BigDecimal.valueOf(2000), "fev", LocalDate.of(2026, 2, 10), user)
        );

        List<MonthSummaryDto> result = reportRepository.findMonthlySummaryByUserId(
                user.getId(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 28)
        );

        assertEquals(2, result.size());
        assertEquals(BigDecimal.ZERO.intValue(), result.get(0).expense().intValue());
    }

    @Test
    void shouldSumMultipleValuesInSameMonth() {

        User user = userRepository.save(
                new User("Pedro", null, "email@test.com", "123", "11912345678", PersonType.PF)
        );

        Supplier supplier = supplierRepository.save(
                new Supplier("Supplier A", null, user)
        );

        incomeRepository.save(
                new Income(BigDecimal.valueOf(1000), "i1", LocalDate.of(2026, 2, 10), user)
        );

        incomeRepository.save(
                new Income(BigDecimal.valueOf(500), "i2", LocalDate.of(2026, 2, 15), user)
        );

        Expense expense1 = new Expense("e1", PaymentType.CASH, LocalDate.of(2026, 2, 10), user, supplier);
        expense1.generateInstallments(BigDecimal.valueOf(300), Map.of(0, ""));

        Expense expense2 = new Expense("e2", PaymentType.CASH, LocalDate.of(2026, 2, 15), user, supplier);
        expense2.generateInstallments(BigDecimal.valueOf(200), Map.of(0, ""));

        expenseRepository.saveAll(List.of(expense1, expense2));

        List<MonthSummaryDto> result = reportRepository.findMonthlySummaryByUserId(
                user.getId(),
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        );

        MonthSummaryDto summary = result.get(0);

        assertEquals(new BigDecimal("1500").floatValue(), summary.income().floatValue());
        assertEquals(new BigDecimal("500").floatValue(), summary.expense().floatValue());
    }
}
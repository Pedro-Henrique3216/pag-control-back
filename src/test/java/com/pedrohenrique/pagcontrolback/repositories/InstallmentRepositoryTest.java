package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.dtos.response.CategorySummaryDto;
import com.pedrohenrique.pagcontrolback.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class InstallmentRepositoryTest {

    @Autowired
    private InstallmentRepository installmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Should sum paid installments by date range")
    void shouldSumPaidInstallments() {
        User user = userRepository.save(new User("Pedro", null, "email@test.com", "123", "1112345678", PersonType.PF));

        Supplier supplier = supplierRepository.save(new Supplier("Supplier A", null, user));

        Expense expense = expenseRepository.save(
                new Expense("teste", PaymentType.CASH, LocalDate.now(), user, supplier)
        );

        expense.generateInstallments(BigDecimal.valueOf(100), null);

        Expense expense2 = expenseRepository.save(
                new Expense("teste2", PaymentType.CASH, LocalDate.now(), user, supplier)
        );

        expense2.generateInstallments(BigDecimal.valueOf(200), null);

        expenseRepository.saveAll(List.of(expense, expense2));

        BigDecimal result = installmentRepository.sumPaidByUserIdAndDateBetween(
                user.getId(),
                LocalDate.now().withDayOfMonth(1),
                LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
        );

        assertEquals(new BigDecimal("300").floatValue(), result.floatValue());
    }

    @Test
    void shouldReturnZeroWhenNoPaidInstallments() {
        User user = userRepository.save(new User("Pedro", null, "email@test.com", "123", "1112345678", PersonType.PF));

        BigDecimal result = installmentRepository.sumPaidByUserIdAndDateBetween(
                user.getId(),
                LocalDate.now().withDayOfMonth(1),
                LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
        );

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void shouldGroupByCategory() {
        User user = userRepository.save(new User("Pedro", null, "email@test.com", "123", "1112345678", PersonType.PF));

        Category category = categoryRepository.save(new Category("Food", CategoryType.EXPENSE, user));

        Supplier supplier = supplierRepository.save(new Supplier("Supplier A", null, user));

        Expense expense = new Expense("teste", PaymentType.CASH, LocalDate.now(), user, supplier);

        expense.assignCategory(category);

        expense.generateInstallments(BigDecimal.valueOf(100), null);

        expenseRepository.save(expense);

        List<CategorySummaryDto> result = installmentRepository.sumByCategory(
                user.getId(),
                LocalDate.now().withDayOfMonth(1),
                LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
        );

        assertEquals(1, result.size());
        assertEquals("food", result.get(0).description());
        assertEquals(new BigDecimal("100").floatValue(), result.get(0).total().floatValue());
    }

    @Test
    void shouldReturnOutrosWhenCategoryIsNull() {
        User user = userRepository.save(new User("Pedro", null, "email@test.com", "123", "1112345678", PersonType.PF));

        Supplier supplier = supplierRepository.save(new Supplier("Supplier A", null, user));

        Expense expense = new Expense("teste", PaymentType.CASH, LocalDate.now(), user, supplier);

        expense.generateInstallments(BigDecimal.valueOf(100), null);

        expenseRepository.save(expense);

        List<CategorySummaryDto> result = installmentRepository.sumByCategory(
                user.getId(),
                LocalDate.now().withDayOfMonth(1),
                LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
        );

        assertEquals("outros", result.get(0).description());
    }

    @Test
    void shouldSumOverdue() {
        User user = userRepository.save(new User("Pedro", null, "email@test.com", "123", "1112345678", PersonType.PF));

        Supplier supplier = supplierRepository.save(new Supplier("Supplier A", null, user));

        Expense expense = new Expense("teste", PaymentType.BILL, LocalDate.now().minusDays(2), user, supplier);

        expense.generateInstallments(BigDecimal.valueOf(150), Map.of(1, ""));

        Expense expense2 = new Expense("teste2", PaymentType.BILL, LocalDate.now().minusDays(20), user, supplier);

        expense2.generateInstallments(BigDecimal.valueOf(200), Map.of(1, ""));

        Expense expense3 = new Expense("teste3", PaymentType.BILL, LocalDate.now(), user, supplier);

        expense3.generateInstallments(BigDecimal.valueOf(200), Map.of(1, ""));

        expenseRepository.saveAll(List.of(expense, expense2, expense3));

        BigDecimal result = installmentRepository.sumOverdueByUser(user.getId());

        assertEquals(new BigDecimal("350").floatValue(), result.floatValue());
    }

    @Test
    void shouldCountOverdue() {
        User user = userRepository.save(new User("Pedro", null, "email@test.com", "123", "1112345678", PersonType.PF));

        Supplier supplier = supplierRepository.save(new Supplier("Supplier A", null, user));

        Expense expense = new Expense("teste", PaymentType.BILL, LocalDate.now().minusDays(2), user, supplier);

        expense.generateInstallments(BigDecimal.valueOf(150), Map.of(1, ""));

        Expense expense2 = new Expense("teste2", PaymentType.BILL, LocalDate.now().minusDays(20), user, supplier);

        expense2.generateInstallments(BigDecimal.valueOf(200), Map.of(1, ""));

        Expense expense3 = new Expense("teste3", PaymentType.BILL, LocalDate.now(), user, supplier);

        expense3.generateInstallments(BigDecimal.valueOf(200), Map.of(1, ""));

        expenseRepository.saveAll(List.of(expense, expense2, expense3));

        Integer result = installmentRepository.countOverdueByUser(user.getId());

        assertEquals(2, result);
    }

    @Test
    void shouldSumUpcoming() {
        User user = userRepository.save(new User("Pedro", null, "email@test.com", "123", "1112345678", PersonType.PF));

        Supplier supplier = supplierRepository.save(new Supplier("Supplier A", null, user));

        Expense expense = new Expense("teste", PaymentType.CREDIT, LocalDate.now(), user, supplier);

        expense.generateInstallments(BigDecimal.valueOf(200), Map.of(7, ""));

        Expense expense2 = new Expense("teste2", PaymentType.CREDIT, LocalDate.now(), user, supplier);

        expense2.generateInstallments(BigDecimal.valueOf(250), Map.of(1, ""));

        expenseRepository.saveAll(List.of(expense, expense2));

        BigDecimal result = installmentRepository.sumUpcomingByUser(
                user.getId(),
                LocalDate.now().plusDays(7)
        );

        assertEquals(new BigDecimal("450").floatValue(), result.floatValue());
    }

    @Test
    void shouldCountUpcoming() {
        User user = userRepository.save(new User("Pedro", null, "email@test.com", "123", "1112345678", PersonType.PF));

        Supplier supplier = supplierRepository.save(new Supplier("Supplier A", null, user));

        Expense expense = new Expense("teste", PaymentType.CREDIT, LocalDate.now(), user, supplier);

        expense.generateInstallments(BigDecimal.valueOf(200), Map.of(7, ""));

        expenseRepository.save(expense);

        Integer result = installmentRepository.countUpcomingByUser(
                user.getId(),
                LocalDate.now().plusDays(7)
        );

        assertEquals(1, result);
    }
}
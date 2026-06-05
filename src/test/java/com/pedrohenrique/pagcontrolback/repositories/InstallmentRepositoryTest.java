package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.response.CategorySummaryDto;
import com.pedrohenrique.pagcontrolback.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    private User createUser(
            String email
    ) {

        User user = new User(
                "Pedro",
                null,
                email,
                "123",
                "11912345678",
                PersonType.PF
        );

        return userRepository.save(user);
    }

    private Supplier createSupplier(
            User user,
            String name
    ) {

        Supplier supplier = new Supplier(
                name,
                null,
                user
        );

        return supplierRepository.save(supplier);
    }

    private Category createCategory(
            User user,
            String name
    ) {

        Category category = new Category(
                name,
                TransactionType.EXPENSE,
                user
        );

        return categoryRepository.save(category);
    }

    private Expense createExpense(
            User user,
            Supplier supplier,
            Category category,
            String description,
            PaymentType paymentType,
            LocalDate date,
            BigDecimal amount,
            Map<Integer, String> installments
    ) {

        Expense expense = new Expense(
                "INV-" + System.nanoTime(),
                description,
                paymentType,
                date,
                user,
                new Money(amount)
        );

        expense.setSupplier(supplier);

        if (category != null) {
            expense.assignCategory(category);
        }

        expense.generateInstallments(installments);

        return expenseRepository.save(expense);
    }

    @Nested
    class SumPaidTests {

        @Test
        @DisplayName("Should sum paid installments by date range")
        void shouldSumPaidInstallments() {

            User user = createUser("sum@test.com");

            Supplier supplier = createSupplier(
                    user,
                    "Supplier A"
            );

            createExpense(
                    user,
                    supplier,
                    null,
                    "teste",
                    PaymentType.CASH,
                    LocalDate.now(),
                    BigDecimal.valueOf(100),
                    Map.of(0, "")
            );

            createExpense(
                    user,
                    supplier,
                    null,
                    "teste2",
                    PaymentType.CASH,
                    LocalDate.now(),
                    BigDecimal.valueOf(200),
                    Map.of(0, "")
            );

            BigDecimal result =
                    installmentRepository.sumPaidByUserIdAndDateBetween(
                            user.getId(),
                            LocalDate.now().withDayOfMonth(1),
                            LocalDate.now().withDayOfMonth(
                                    LocalDate.now().lengthOfMonth()
                            )
                    );

            assertEquals(
                    new BigDecimal("300").floatValue(),
                    result.floatValue()
            );
        }

        @Test
        @DisplayName("Should return zero when no paid installments")
        void shouldReturnZeroWhenNoPaidInstallments() {

            User user = createUser("zero@test.com");

            BigDecimal result =
                    installmentRepository.sumPaidByUserIdAndDateBetween(
                            user.getId(),
                            LocalDate.now().withDayOfMonth(1),
                            LocalDate.now().withDayOfMonth(
                                    LocalDate.now().lengthOfMonth()
                            )
                    );

            assertEquals(
                    BigDecimal.ZERO.floatValue(),
                    result.floatValue()
            );
        }
    }

    @Nested
    class CategorySummaryTests {

        @Test
        void shouldGroupByCategory() {

            User user = createUser("category@test.com");

            Category category = createCategory(
                    user,
                    "Food"
            );

            Supplier supplier = createSupplier(
                    user,
                    "Supplier A"
            );

            createExpense(
                    user,
                    supplier,
                    category,
                    "teste",
                    PaymentType.CASH,
                    LocalDate.now(),
                    BigDecimal.valueOf(100),
                    Map.of(0, "")
            );

            List<CategorySummaryDto> result =
                    installmentRepository.sumByCategory(
                            user.getId(),
                            LocalDate.now().withDayOfMonth(1),
                            LocalDate.now().withDayOfMonth(
                                    LocalDate.now().lengthOfMonth()
                            )
                    );

            assertEquals(1, result.size());

            assertEquals(
                    "food",
                    result.get(0).description()
            );

            assertEquals(
                    new BigDecimal("100").floatValue(),
                    result.get(0).total().floatValue()
            );
        }

        @Test
        void shouldReturnOutrosWhenCategoryIsNull() {

            User user = createUser("outros@test.com");

            Supplier supplier = createSupplier(
                    user,
                    "Supplier A"
            );

            createExpense(
                    user,
                    supplier,
                    null,
                    "teste",
                    PaymentType.CASH,
                    LocalDate.now(),
                    BigDecimal.valueOf(100),
                    Map.of(0, "")
            );

            List<CategorySummaryDto> result =
                    installmentRepository.sumByCategory(
                            user.getId(),
                            LocalDate.now().withDayOfMonth(1),
                            LocalDate.now().withDayOfMonth(
                                    LocalDate.now().lengthOfMonth()
                            )
                    );

            assertEquals(
                    "outros",
                    result.get(0).description()
            );
        }
    }

    @Nested
    class OverdueTests {

        @Test
        void shouldSumOverdue() {

            User user = createUser("overdue@test.com");

            Supplier supplier = createSupplier(
                    user,
                    "Supplier A"
            );

            createExpense(
                    user,
                    supplier,
                    null,
                    "teste",
                    PaymentType.BILL,
                    LocalDate.now().minusDays(30),
                    BigDecimal.valueOf(150),
                    Map.of(1, "")
            );

            createExpense(
                    user,
                    supplier,
                    null,
                    "teste2",
                    PaymentType.BILL,
                    LocalDate.now().minusDays(30),
                    BigDecimal.valueOf(200),
                    Map.of(1, "")
            );

            createExpense(
                    user,
                    supplier,
                    null,
                    "teste3",
                    PaymentType.BILL,
                    LocalDate.now().minusDays(30),
                    BigDecimal.valueOf(200),
                    Map.of(30, "")
            );

            BigDecimal result =
                    installmentRepository.sumOverdueByUser(
                            user.getId()
                    );

            assertEquals(
                    new BigDecimal("350").floatValue(),
                    result.floatValue()
            );
        }

        @Test
        void shouldCountOverdue() {

            User user = createUser("count-overdue@test.com");

            Supplier supplier = createSupplier(
                    user,
                    "Supplier A"
            );

            createExpense(
                    user,
                    supplier,
                    null,
                    "teste",
                    PaymentType.BILL,
                    LocalDate.now().minusDays(30),
                    BigDecimal.valueOf(150),
                    Map.of(1, "")
            );

            createExpense(
                    user,
                    supplier,
                    null,
                    "teste2",
                    PaymentType.BILL,
                    LocalDate.now().minusDays(30),
                    BigDecimal.valueOf(200),
                    Map.of(1, "")
            );

            createExpense(
                    user,
                    supplier,
                    null,
                    "teste3",
                    PaymentType.BILL,
                    LocalDate.now(),
                    BigDecimal.valueOf(200),
                    Map.of(30, "")
            );

            Integer result =
                    installmentRepository.countOverdueByUser(
                            user.getId()
                    );

            assertEquals(2, result);
        }
    }

    @Nested
    class UpcomingTests {

        @Test
        void shouldSumUpcoming() {

            User user = createUser("upcoming@test.com");

            Supplier supplier = createSupplier(
                    user,
                    "Supplier A"
            );

            createExpense(
                    user,
                    supplier,
                    null,
                    "teste",
                    PaymentType.CREDIT,
                    LocalDate.now(),
                    BigDecimal.valueOf(200),
                    Map.of(7, "")
            );

            createExpense(
                    user,
                    supplier,
                    null,
                    "teste2",
                    PaymentType.CREDIT,
                    LocalDate.now(),
                    BigDecimal.valueOf(250),
                    Map.of(1, "")
            );

            BigDecimal result =
                    installmentRepository.sumUpcomingByUser(
                            user.getId(),
                            LocalDate.now().plusDays(7)
                    );

            assertEquals(
                    new BigDecimal("450").floatValue(),
                    result.floatValue()
            );
        }

        @Test
        void shouldCountUpcoming() {

            User user = createUser("count-upcoming@test.com");

            Supplier supplier = createSupplier(
                    user,
                    "Supplier A"
            );

            createExpense(
                    user,
                    supplier,
                    null,
                    "teste",
                    PaymentType.CREDIT,
                    LocalDate.now(),
                    BigDecimal.valueOf(200),
                    Map.of(7, "")
            );

            Integer result =
                    installmentRepository.countUpcomingByUser(
                            user.getId(),
                            LocalDate.now().plusDays(7)
                    );

            assertEquals(1, result);
        }
    }
}
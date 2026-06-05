package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.request.ListInstallmentQuery;
import com.pedrohenrique.pagcontrolback.model.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class InstallmentRepositoryImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private InstallmentRepositoryImpl repository;

    private User createUser(String email) {

        User user = new User(
                "John",
                null,
                email,
                "123",
                "12345678900",
                PersonType.PF
        );

        em.persist(user);

        return user;
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

        supplier.setUser(user);

        em.persist(supplier);

        return supplier;
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

        em.persist(category);

        return category;
    }

    private Expense createExpense(
            User user,
            Supplier supplier,
            Category category,
            String description,
            PaymentType paymentType
    ) {

        Expense expense = new Expense(
                "INV-" + UUID.randomUUID(),
                description,
                paymentType,
                LocalDate.now(),
                user,
                new Money(BigDecimal.valueOf(100))
        );

        expense.setSupplier(supplier);

        expense.assignCategory(category);

        em.persist(expense);

        return expense;
    }

    private Installment createInstallment(
            Expense expense,
            LocalDate dueDate,
            InstallmentStatus status
    ) {

        Installment installment = new Installment(
                Money.of(BigDecimal.valueOf(100)),
                dueDate,
                null,
                expense,
                1,
                1
        );

        if (status == InstallmentStatus.PAID) {
            installment.markAsPaid();
        }

        em.persist(installment);

        return installment;
    }

    @Nested
    class SearchTests {

        @Test
        void shouldReturnOnlyInstallmentsFromUser() {

            User user1 = createUser("user1@test.com");

            Supplier supplier1 = createSupplier(
                    user1,
                    "Supplier 1"
            );

            Category category1 = createCategory(
                    user1,
                    "Food"
            );

            Expense expense1 = createExpense(
                    user1,
                    supplier1,
                    category1,
                    "Expense 1",
                    PaymentType.CREDIT
            );

            createInstallment(
                    expense1,
                    LocalDate.now(),
                    InstallmentStatus.UNPAID
            );

            User user2 = createUser("user2@test.com");

            Supplier supplier2 = createSupplier(
                    user2,
                    "Supplier 2"
            );

            Category category2 = createCategory(
                    user2,
                    "Transport"
            );

            Expense expense2 = createExpense(
                    user2,
                    supplier2,
                    category2,
                    "Expense 2",
                    PaymentType.CREDIT
            );

            createInstallment(
                    expense2,
                    LocalDate.now(),
                    InstallmentStatus.UNPAID
            );

            em.flush();
            em.clear();

            var query = new ListInstallmentQuery(
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    null,
                    null
            );

            List<Installment> result =
                    repository.search(query, user1.getId());

            assertEquals(1, result.size());
        }

        @Test
        void shouldFilterBySupplier() {

            User user = createUser("supplier@test.com");

            Supplier supplier1 = createSupplier(user, "S1");

            Supplier supplier2 = createSupplier(user, "S2");

            Category category = createCategory(user, "Food");

            Expense expense1 = createExpense(
                    user,
                    supplier1,
                    category,
                    "Expense 1",
                    PaymentType.CREDIT
            );

            Expense expense2 = createExpense(
                    user,
                    supplier2,
                    category,
                    "Expense 2",
                    PaymentType.CREDIT
            );

            createInstallment(
                    expense1,
                    LocalDate.now(),
                    InstallmentStatus.UNPAID
            );

            createInstallment(
                    expense2,
                    LocalDate.now(),
                    InstallmentStatus.UNPAID
            );

            em.flush();
            em.clear();

            var query = new ListInstallmentQuery(
                    null,
                    null,
                    supplier1.getId(),
                    null,
                    false,
                    false,
                    null,
                    null
            );

            List<Installment> result =
                    repository.search(query, user.getId());

            assertEquals(1, result.size());
        }

        @Test
        void shouldFilterByMonth() {

            User user = createUser("month@test.com");

            Supplier supplier = createSupplier(user, "S1");

            Category category = createCategory(user, "Food");

            Expense expense = createExpense(
                    user,
                    supplier,
                    category,
                    "Expense",
                    PaymentType.CREDIT
            );

            createInstallment(
                    expense,
                    LocalDate.of(2026, 2, 10),
                    InstallmentStatus.UNPAID
            );

            createInstallment(
                    expense,
                    LocalDate.of(2026, 3, 10),
                    InstallmentStatus.UNPAID
            );

            em.flush();
            em.clear();

            var query = new ListInstallmentQuery(
                    null,
                    YearMonth.of(2026, 2),
                    null,
                    null,
                    false,
                    false,
                    null,
                    null
            );

            List<Installment> result =
                    repository.search(query, user.getId());

            assertEquals(1, result.size());
        }

        @Test
        void shouldFilterByStatus() {

            User user = createUser("status@test.com");

            Supplier supplier = createSupplier(user, "S1");

            Category category = createCategory(user, "Food");

            Expense expense = createExpense(
                    user,
                    supplier,
                    category,
                    "Expense",
                    PaymentType.CREDIT
            );

            createInstallment(
                    expense,
                    LocalDate.now(),
                    InstallmentStatus.PAID
            );

            createInstallment(
                    expense,
                    LocalDate.now(),
                    InstallmentStatus.UNPAID
            );

            em.flush();
            em.clear();

            var query = new ListInstallmentQuery(
                    null,
                    null,
                    null,
                    InstallmentStatus.PAID,
                    false,
                    false,
                    null,
                    null
            );

            List<Installment> result =
                    repository.search(query, user.getId());

            assertEquals(1, result.size());

            assertEquals(
                    InstallmentStatus.PAID,
                    result.get(0).getStatus()
            );
        }

        @Test
        void shouldReturnOnlyOverdueInstallments() {

            User user = createUser("overdue@test.com");

            Supplier supplier = createSupplier(user, "S1");

            Category category = createCategory(user, "Food");

            Expense expense = createExpense(
                    user,
                    supplier,
                    category,
                    "Expense",
                    PaymentType.CREDIT
            );

            createInstallment(
                    expense,
                    LocalDate.now().minusDays(5),
                    InstallmentStatus.UNPAID
            );

            createInstallment(
                    expense,
                    LocalDate.now().plusDays(5),
                    InstallmentStatus.UNPAID
            );

            em.flush();
            em.clear();

            var query = new ListInstallmentQuery(
                    null,
                    null,
                    null,
                    null,
                    true,
                    false,
                    null,
                    null
            );

            List<Installment> result =
                    repository.search(query, user.getId());

            assertEquals(1, result.size());

            assertTrue(
                    result.get(0)
                            .getDueDate()
                            .isBefore(LocalDate.now())
            );
        }

        @Test
        void shouldReturnInstallmentsDueInNext7Days() {

            User user = createUser("next7@test.com");

            Supplier supplier = createSupplier(user, "S1");

            Category category = createCategory(user, "Food");

            Expense expense = createExpense(
                    user,
                    supplier,
                    category,
                    "Expense",
                    PaymentType.CREDIT
            );

            createInstallment(
                    expense,
                    LocalDate.now().plusDays(3),
                    InstallmentStatus.UNPAID
            );

            createInstallment(
                    expense,
                    LocalDate.now().plusDays(10),
                    InstallmentStatus.UNPAID
            );

            em.flush();
            em.clear();

            var query = new ListInstallmentQuery(
                    null,
                    null,
                    null,
                    null,
                    false,
                    true,
                    null,
                    null
            );

            List<Installment> result =
                    repository.search(query, user.getId());

            assertEquals(1, result.size());
        }

        @Test
        void shouldFilterByPaymentType() {

            User user = createUser("payment@test.com");

            Supplier supplier = createSupplier(user, "S1");

            Category category = createCategory(user, "Food");

            Expense creditExpense = createExpense(
                    user,
                    supplier,
                    category,
                    "Credit Expense",
                    PaymentType.CREDIT
            );

            Expense cashExpense = createExpense(
                    user,
                    supplier,
                    category,
                    "Cash Expense",
                    PaymentType.CASH
            );

            createInstallment(
                    creditExpense,
                    LocalDate.now(),
                    InstallmentStatus.UNPAID
            );

            createInstallment(
                    cashExpense,
                    LocalDate.now(),
                    InstallmentStatus.UNPAID
            );

            em.flush();
            em.clear();

            var query = new ListInstallmentQuery(
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    PaymentType.CREDIT,
                    null
            );

            List<Installment> result =
                    repository.search(query, user.getId());

            assertEquals(1, result.size());

            assertEquals(
                    PaymentType.CREDIT,
                    result.get(0)
                            .getExpense()
                            .getPaymentType()
            );
        }

        @Test
        void shouldFilterByCategory() {

            User user = createUser("category@test.com");

            Supplier supplier = createSupplier(user, "S1");

            Category food = createCategory(user, "Food");

            Category transport = createCategory(user, "Transport");

            Expense expense1 = createExpense(
                    user,
                    supplier,
                    food,
                    "Food Expense",
                    PaymentType.CREDIT
            );

            Expense expense2 = createExpense(
                    user,
                    supplier,
                    transport,
                    "Transport Expense",
                    PaymentType.CREDIT
            );

            createInstallment(
                    expense1,
                    LocalDate.now(),
                    InstallmentStatus.UNPAID
            );

            createInstallment(
                    expense2,
                    LocalDate.now(),
                    InstallmentStatus.UNPAID
            );

            em.flush();
            em.clear();

            var query = new ListInstallmentQuery(
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    null,
                    food.getId()
            );

            List<Installment> result =
                    repository.search(query, user.getId());

            assertEquals(1, result.size());

            assertEquals(
                    food.getId(),
                    result.get(0)
                            .getExpense()
                            .getCategory()
                            .getId()
            );
        }

        @Test
        void shouldReturnEmptyListWhenNoInstallmentsMatch() {

            User user = createUser("empty@test.com");

            var query = new ListInstallmentQuery(
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    null,
                    null
            );

            List<Installment> result =
                    repository.search(query, user.getId());

            assertNotNull(result);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class OrderingTests {

        @Test
        void shouldOrderInstallmentsByDueDateAscending() {

            User user = createUser("order@test.com");

            Supplier supplier = createSupplier(user, "S1");

            Category category = createCategory(user, "Food");

            Expense expense = createExpense(
                    user,
                    supplier,
                    category,
                    "Expense",
                    PaymentType.CREDIT
            );

            createInstallment(
                    expense,
                    LocalDate.now().plusDays(10),
                    InstallmentStatus.UNPAID
            );

            createInstallment(
                    expense,
                    LocalDate.now().plusDays(2),
                    InstallmentStatus.UNPAID
            );

            createInstallment(
                    expense,
                    LocalDate.now().plusDays(5),
                    InstallmentStatus.UNPAID
            );

            em.flush();
            em.clear();

            var query = new ListInstallmentQuery(
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    null,
                    null
            );

            List<Installment> result =
                    repository.search(query, user.getId());

            assertEquals(3, result.size());

            assertTrue(
                    result.get(0).getDueDate()
                            .isBefore(result.get(1).getDueDate())
            );

            assertTrue(
                    result.get(1).getDueDate()
                            .isBefore(result.get(2).getDueDate())
            );
        }
    }
}
package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.dtos.request.ListExpensesQuery;
import com.pedrohenrique.pagcontrolback.model.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ExpenseRepositoryImplTest {

    @Autowired
    private ExpenseRepositoryCustom expenseRepository;

    @Autowired
    private EntityManager em;

    private User createUser(String email) {
        User user = new User(
                "User",
                null,
                email,
                "123456",
                "12345678900",
                PersonType.PF
        );
        em.persist(user);
        return user;
    }

    private Supplier createSupplier(User user, String name) {
        Supplier supplier = new Supplier(name);
        supplier.setUser(user);
        em.persist(supplier);
        return supplier;
    }

    private Expense createExpense(
            User user,
            Supplier supplier,
            String invoice,
            LocalDate date
    ) {
        Expense expense = new Expense(
                invoice,
                PaymentType.CASH,
                date
        );
        expense.setUser(user);
        expense.setSupplier(supplier);
        em.persist(expense);
        return expense;
    }

    @Test
    void shouldReturnAllExpensesFromUser_whenNoFilterIsProvided() {

        User user = createUser("a@test.com");
        Supplier supplier = createSupplier(user, "Fornecedor");

        createExpense(user, supplier, "INV-1", LocalDate.of(2026, 1, 10));
        createExpense(user, supplier, "INV-2", LocalDate.of(2026, 2, 5));

        em.flush();
        em.clear();

        var query = new ListExpensesQuery(
                null,
                null,
                null
        );

        List<Expense> result =
                expenseRepository.search(query, user.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldFilterExpensesByExpenseMonth() {

        User user = createUser("b@test.com");
        Supplier supplier = createSupplier(user, "Fornecedor");

        createExpense(user, supplier, "INV-1",
                LocalDate.of(2026, 1, 5));

        createExpense(user, supplier, "INV-2",
                LocalDate.of(2026, 2, 5));

        em.flush();
        em.clear();

        var query = new ListExpensesQuery(
                YearMonth.of(2026, 2),
                null,
                null
        );

        List<Expense> result =
                expenseRepository.search(query, user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInvoiceNumber()).isEqualTo("INV-2");
    }

    @Test
    void shouldFilterExpensesBySupplierId() {

        User user = createUser("c@test.com");

        Supplier supplier1 = createSupplier(user, "Fornecedor 1");
        Supplier supplier2 = createSupplier(user, "Fornecedor 2");

        createExpense(user, supplier1, "INV-1",
                LocalDate.of(2026, 1, 5));

        createExpense(user, supplier2, "INV-2",
                LocalDate.of(2026, 2, 5));

        em.flush();
        em.clear();

        var query = new ListExpensesQuery(
                null,
                supplier1.getId(),
                null
        );

        List<Expense> result =
                expenseRepository.search(query, user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSupplier().getId())
                .isEqualTo(supplier1.getId());
    }

    @Test
    void shouldFilterExpensesByInvoiceNumber() {

        User user = createUser("d@test.com");
        Supplier supplier = createSupplier(user, "Fornecedor");

        createExpense(user, supplier, "NF-001",
                LocalDate.of(2026, 2, 5));

        createExpense(user, supplier, "NF-002",
                LocalDate.of(2026, 2, 5));

        em.flush();
        em.clear();

        var query = new ListExpensesQuery(
                null,
                null,
                "NF-001"
        );

        List<Expense> result =
                expenseRepository.search(query, user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInvoiceNumber())
                .isEqualTo("NF-001");
    }

    @Test
    void shouldFilterExpensesBySupplierAndInvoiceAndMonth() {

        User user = createUser("e@test.com");

        Supplier supplier = createSupplier(user, "Fornecedor");

        createExpense(user, supplier, "INV-FAIL",
                LocalDate.of(2026, 1, 5));

        createExpense(user, supplier, "INV-OK",
                LocalDate.of(2026, 2, 5));

        em.flush();
        em.clear();

        var query = new ListExpensesQuery(
                YearMonth.of(2026, 2),
                supplier.getId(),
                "INV-OK"
        );

        List<Expense> result =
                expenseRepository.search(query, user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInvoiceNumber())
                .isEqualTo("INV-OK");
    }

    @Test
    void shouldReturnOnlyExpensesFromGivenUser() {

        User user1 = createUser("f1@test.com");
        User user2 = createUser("f2@test.com");

        Supplier supplier1 = createSupplier(user1, "Fornecedor 1");
        Supplier supplier2 = createSupplier(user2, "Fornecedor 2");

        createExpense(user1, supplier1, "INV-USER1",
                LocalDate.of(2026, 2, 5));

        createExpense(user2, supplier2, "INV-USER2",
                LocalDate.of(2026, 2, 5));

        em.flush();
        em.clear();

        var query = new ListExpensesQuery(
                null,
                null,
                null
        );

        List<Expense> result =
                expenseRepository.search(query, user1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId())
                .isEqualTo(user1.getId());
    }

}

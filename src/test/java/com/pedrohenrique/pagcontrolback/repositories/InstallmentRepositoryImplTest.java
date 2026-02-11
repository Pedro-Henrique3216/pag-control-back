package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.dtos.request.ListInstallmentQuery;
import com.pedrohenrique.pagcontrolback.model.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class InstallmentRepositoryImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private InstallmentRepositoryImpl repository;

    private User createUser() {
        return em.merge(new User(
                "John",
                null,
                "john@test.com",
                "123",
                "12345678900",
                PersonType.PF
        ));
    }

    private Supplier createSupplier(User user, String name) {
        Supplier s = new Supplier(name);
        s.setUser(user);
        em.persist(s);
        return s;
    }

    private Expense createExpense(User user, Supplier supplier) {
        Expense e = new Expense(
                "INV-" + UUID.randomUUID(),
                PaymentType.CREDIT,
                LocalDate.now()
        );
        e.setUser(user);
        e.setSupplier(supplier);
        em.persist(e);
        return e;
    }

    private void createInstallment(
            Expense expense,
            LocalDate dueDate,
            InstallmentStatus status
    ) {
        Installment i = new Installment(
                BigDecimal.valueOf(100),
                dueDate,
                null
        );
        if(status.equals(InstallmentStatus.PAID)){
            i.markAsPaid();
        }
        i.setExpense(expense);
        em.persist(i);
    }


    @Test
    void shouldReturnOnlyInstallmentsFromUser() {

        User user1 = createUser();
        Supplier s1 = createSupplier(user1, "S1");
        Expense e1 = createExpense(user1, s1);

        createInstallment(e1, LocalDate.now(), InstallmentStatus.UNPAID);

        User user2 = em.merge(new User(
                "Mary",
                null,
                "mary@test.com",
                "123",
                "99999999999",
                PersonType.PF
        ));
        Supplier s2 = createSupplier(user2, "S2");
        Expense e2 = createExpense(user2, s2);

        createInstallment(e2, LocalDate.now(), InstallmentStatus.UNPAID);

        em.flush();
        em.clear();

        var query = new ListInstallmentQuery(
                null, null, null, null, null
        );

        List<Installment> result =
                repository.search(query, user1.getId());

        assertEquals(1, result.size());
    }

    @Test
    void shouldFilterBySupplier() {

        User user = createUser();

        Supplier supplier1 = createSupplier(user, "S1");
        Supplier supplier2 = createSupplier(user, "S2");

        Expense e1 = createExpense(user, supplier1);
        Expense e2 = createExpense(user, supplier2);

        createInstallment(e1, LocalDate.now(), InstallmentStatus.UNPAID);
        createInstallment(e2, LocalDate.now(), InstallmentStatus.UNPAID);

        em.flush();
        em.clear();

        var query = new ListInstallmentQuery(
                null,
                supplier1.getId(),
                null,
                null,
                null
        );

        var result = repository.search(query, user.getId());

        assertEquals(1, result.size());
    }

    @Test
    void shouldFilterByMonth() {

        User user = createUser();
        Supplier supplier = createSupplier(user, "S1");
        Expense expense = createExpense(user, supplier);

        createInstallment(expense,
                LocalDate.of(2026, 2, 10),
                InstallmentStatus.UNPAID);

        createInstallment(expense,
                LocalDate.of(2026, 3, 10),
                InstallmentStatus.UNPAID);

        em.flush();
        em.clear();

        var query = new ListInstallmentQuery(
                YearMonth.of(2026, 2),
                null,
                null,
                null,
                null
        );

        var result = repository.search(query, user.getId());

        assertEquals(1, result.size());
    }

    @Test
    void shouldFilterByStatus() {

        User user = createUser();
        Supplier supplier = createSupplier(user, "S1");
        Expense expense = createExpense(user, supplier);

        createInstallment(expense,
                LocalDate.now(),
                InstallmentStatus.PAID);

        createInstallment(expense,
                LocalDate.now(),
                InstallmentStatus.UNPAID);

        em.flush();
        em.clear();

        var query = new ListInstallmentQuery(
                null,
                null,
                InstallmentStatus.PAID,
                null,
                null
        );

        var result = repository.search(query, user.getId());

        assertEquals(1, result.size());
        assertEquals(InstallmentStatus.PAID, result.get(0).getStatus());
    }

    @Test
    void shouldReturnOnlyOverdueInstallments() {

        User user = createUser();
        Supplier supplier = createSupplier(user, "S1");
        Expense expense = createExpense(user, supplier);

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
                true,
                null
        );

        var result = repository.search(query, user.getId());

        assertEquals(1, result.size());
        assertTrue(result.get(0).getDueDate().isBefore(LocalDate.now()));
    }

    @Test
    void shouldReturnInstallmentsDueInNext7Days() {

        User user = createUser();
        Supplier supplier = createSupplier(user, "S1");
        Expense expense = createExpense(user, supplier);

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
                true
        );

        var result = repository.search(query, user.getId());

        assertEquals(1, result.size());
    }

    @Test
    void shouldNotReturnPaidInstallmentsWhenFilteringOverdue() {

        User user = createUser();
        Supplier supplier = createSupplier(user, "S1");
        Expense expense = createExpense(user, supplier);

        createInstallment(
                expense,
                LocalDate.now().minusDays(2),
                InstallmentStatus.PAID
        );

        em.flush();
        em.clear();

        var query = new ListInstallmentQuery(
                null,
                null,
                null,
                true,
                null
        );

        var result = repository.search(query, user.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenNoInstallmentsMatch() {

        User user = createUser();

        var query = new ListInstallmentQuery(
                null, null, null, null, null
        );

        var result = repository.search(query, user.getId());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}

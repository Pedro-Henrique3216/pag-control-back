package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.helpers.TestDataFactory;
import com.pedrohenrique.pagcontrolback.model.PersonType;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InstallmentControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TestDataFactory factory;

    private User user;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/installments";

        expenseRepository.deleteAll();
        userRepository.deleteAll();
        supplierRepository.deleteAll();

        user = userRepository.save(
                new User(
                        "John Doe",
                        null,
                        "testeExpense@gmail.com",
                        "password123",
                        "12345678900",
                        PersonType.PF
                )
        );

        Supplier s = new Supplier("Supplier Inc.");
        s.setUser(user);

        supplier = supplierRepository.save(s);
    }

    @Test
    void whenGetInstallmentsWithoutFilters_thenReturnAllUserInstallments(){
        factory.createExpense(user.getId(), supplier.getId(), "INV-001", port);
        factory.createExpense(user.getId(), supplier.getId(), "INV-002", port);

        RestAssured
                .given()
                .when()
                .get("/{userId}", user.getId())
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(2));
    }

    @Test
    void whenGetInstallmentsFilteredByMonth_thenReturnOnlyInstallmentsFromMonth(){
        factory.createExpense(user.getId(), supplier.getId(), "INV-001", LocalDate.of(2026, 2, 10), port);
        factory.createExpense(user.getId(), supplier.getId(), "INV-002", LocalDate.of(2026, 1, 10), port);

        RestAssured
                .given()
                .queryParam("month", "2026-02")
                .when()
                .get("/{userId}", user.getId())
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));
    }

    @Test
    void whenGetInstallmentsFilteredBySupplier_thenReturnOnlySupplierInstallments(){
        Supplier s = new Supplier("Other Supplier Inc.");
        s.setUser(user);
        Supplier otherSupplier = supplierRepository.save(s);

        factory.createExpense(user.getId(), supplier.getId(), "INV-001", port);
        factory.createExpense(user.getId(), otherSupplier.getId(), "INV-002", port);

        RestAssured
                .given()
                .queryParam("supplier_id", supplier.getId())
                .when()
                .get("/{userId}", user.getId())
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));
    }

    @Test
    void whenGetInstallmentsFilteredByStatus_thenReturnOnlyInstallmentsWithStatus(){
        factory.createExpense(user.getId(), supplier.getId(), "INV-001", BigDecimal.valueOf(300), port);
        factory.createExpense(user.getId(), supplier.getId(), "INV-002", port);

        RestAssured
                .given()
                .queryParam("status", "UNPAID")
                .when()
                .get("/{userId}", user.getId())
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(2));
    }

    @Test
    void whenGetInstallmentsWithOverdueTrue_thenReturnOnlyOverdueInstallments(){
        factory.createExpense(user.getId(), supplier.getId(), "INV-001", BigDecimal.valueOf(300), LocalDate.now().minusDays(20), port);
        factory.createExpense(user.getId(), supplier.getId(), "INV-002", BigDecimal.valueOf(300), port);

        RestAssured
                .given()
                .queryParam("overdue", true)
                .when()
                .get("/{userId}", user.getId())
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(2));
    }

    @Test
    void whenGetInstallmentsWithDueInNext7DaysTrue_thenReturnOnlyInstallmentsDueInNext7Days(){
        factory.createExpense(user.getId(), supplier.getId(), "INV-001", BigDecimal.valueOf(300), LocalDate.now().minusDays(10), port);
        factory.createExpense(user.getId(), supplier.getId(), "INV-002", BigDecimal.valueOf(300), LocalDate.now(), port);

        RestAssured
                .given()
                .queryParam("due_in_next_days", true)
                .when()
                .get("/{userId}", user.getId())
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(2));
    }

    @Test
    void whenGetInstallmentsWithMultipleFilters_thenReturnOnlyMatchingInstallments(){
        Supplier s = new Supplier("Other Supplier Inc.");
        s.setUser(user);
        Supplier otherSupplier = supplierRepository.save(s);

        factory.createExpense(user.getId(), supplier.getId(), "INV-001", BigDecimal.valueOf(300), LocalDate.now().minusDays(10), port);
        factory.createExpense(user.getId(), otherSupplier.getId(), "INV-002", BigDecimal.valueOf(300), LocalDate.now().minusDays(10), port);
        factory.createExpense(user.getId(), supplier.getId(), "INV-003", BigDecimal.valueOf(300), port);

        RestAssured
                .given()
                .queryParam("supplier_id", supplier.getId())
                .queryParam("overdue", true)
                .when()
                .get("/{userId}", user.getId())
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));
    }

    @Test
    void whenGetInstallmentsWithInvalidUser_thenReturn404(){
        RestAssured
                .given()
                .when()
                .get("/{userId}", "00000000-0000-0000-0000-000000000000")
                .then()
                .statusCode(404);
    }

    @Test
    void whenGetInstallmentsWithNonExistingSupplier_thenReturn404(){
        RestAssured
                .given()
                .queryParam("supplier_id", "00000000-0000-0000-0000-000000000000")
                .when()
                .get("/{userId}", user.getId())
                .then()
                .statusCode(404);
    }













}
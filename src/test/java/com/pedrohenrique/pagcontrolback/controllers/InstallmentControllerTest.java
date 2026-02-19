package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.InstallmentUpdateDto;
import com.pedrohenrique.pagcontrolback.helpers.AuthTestFactory;
import com.pedrohenrique.pagcontrolback.helpers.TestDataFactory;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepository;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
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
    private InstallmentRepository installmentRepository;

    @Autowired
    private TestDataFactory factory;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthTestFactory authFactory;

    private User user;
    private Supplier supplier;
    private String token;

    @BeforeEach
    void setUp() {

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/installments";

        installmentRepository.deleteAll();
        expenseRepository.deleteAll();
        supplierRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(
                new User(
                        "John Doe",
                        null,
                        "testeExpense@gmail.com",
                        passwordEncoder.encode("password123"),
                        "12345678900",
                        PersonType.PF
                )
        );

        token = authFactory.loginAndGetToken(
                port,
                "testeExpense@gmail.com",
                "password123"
        );

        Supplier s = new Supplier("Supplier Inc.");
        s.setUser(user);
        supplier = supplierRepository.save(s);
    }

    @Test
    void whenGetInstallmentsWithoutFilters_thenReturnAllUserInstallments() {

        factory.createExpense(supplier.getId(), "INV-001", port, token);
        factory.createExpense(supplier.getId(), "INV-002", port, token);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(2));
    }

    @Test
    void whenGetInstallmentsFilteredByMonth_thenReturnOnlyInstallmentsFromMonth() {

        factory.createExpense(
                supplier.getId(),
                "INV-001",
                LocalDate.of(2026, 2, 10),
                port,
                token
        );

        factory.createExpense(
                supplier.getId(),
                "INV-002",
                LocalDate.of(2026, 1, 10),
                port,
                token
        );

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("month", "2026-02")
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));
    }

    @Test
    void whenGetInstallmentsFilteredBySupplier_thenReturnOnlySupplierInstallments() {

        Supplier s = new Supplier("Other Supplier Inc.");
        s.setUser(user);
        Supplier otherSupplier = supplierRepository.save(s);

        factory.createExpense(supplier.getId(), "INV-001", port, token);
        factory.createExpense(otherSupplier.getId(), "INV-002", port, token);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("supplier_id", supplier.getId())
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));
    }

    @Test
    void whenGetInstallmentsFilteredByStatus_thenReturnOnlyInstallmentsWithStatus() {

        factory.createExpense(
                supplier.getId(),
                "INV-001",
                BigDecimal.valueOf(300),
                port,
                token
        );

        factory.createExpense(
                supplier.getId(),
                "INV-002",
                port,
                token
        );

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("status", "UNPAID")
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(2));
    }

    @Test
    void whenGetInstallmentsWithOverdueTrue_thenReturnOnlyOverdueInstallments() {

        factory.createExpense(
                supplier.getId(),
                "INV-001",
                BigDecimal.valueOf(300),
                LocalDate.now().minusDays(20),
                port,
                token
        );

        factory.createExpense(
                supplier.getId(),
                "INV-002",
                BigDecimal.valueOf(300),
                port,
                token
        );

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("overdue", true)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(2));
    }

    @Test
    void whenGetInstallmentsWithDueInNext7DaysTrue_thenReturnOnlyInstallmentsDueInNext7Days() {

        factory.createExpense(
                supplier.getId(),
                "INV-001",
                BigDecimal.valueOf(300),
                LocalDate.now().minusDays(10),
                port,
                token
        );

        factory.createExpense(
                supplier.getId(),
                "INV-002",
                BigDecimal.valueOf(300),
                LocalDate.now(),
                port,
                token
        );

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("due_in_next_days", true)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(2));
    }

    @Test
    void whenGetInstallmentsWithNonExistingSupplier_thenReturn404() {

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("supplier_id", UUID.randomUUID())
                .when()
                .get()
                .then()
                .statusCode(404);
    }

    @Test
    void shouldPayInstallmentWhenDataIsValid() {

        factory.createExpense(
                supplier.getId(),
                "INV-001",
                BigDecimal.valueOf(300),
                LocalDate.now(),
                port,
                token
        );

        Expense expense = expenseRepository.findAll()
                .stream()
                .filter(ex -> ex.getInvoiceNumber().equals("INV-001"))
                .findFirst()
                .orElseThrow();

        Installment installment = installmentRepository.findAll()
                .stream()
                .filter(i -> i.getExpense().getId().equals(expense.getId()))
                .findFirst()
                .orElseThrow();

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/{installmentId}/pay", installment.getInstallmentId())
                .then()
                .statusCode(200);

        Installment updated =
                installmentRepository.findById(installment.getInstallmentId())
                        .orElseThrow();

        assertEquals(InstallmentStatus.PAID, updated.getStatus());
    }

    @Test
    void shouldPayInstallmentReturn404WhenInstallmentNotFound() {

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/{installmentId}/pay", UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    void shouldPayInstallmentReturn403WhenInstallmentDoesNotBelongToUser() {

        User otherUser = userRepository.save(
                new User(
                        "Jane Doe",
                        null,
                        "teste2@gmail.com",
                        passwordEncoder.encode("password123"),
                        "12345678900",
                        PersonType.PF
                )
        );

        factory.createExpense(
                supplier.getId(),
                "INV-001",
                BigDecimal.valueOf(300),
                LocalDate.now(),
                port,
                token
        );

        Expense expense = expenseRepository.findAll()
                .stream()
                .filter(ex -> ex.getInvoiceNumber().equals("INV-001"))
                .findFirst()
                .orElseThrow();

        Installment installment = installmentRepository.findAll()
                .stream()
                .filter(i -> i.getExpense().getId().equals(expense.getId()))
                .findFirst()
                .orElseThrow();

        String otherToken = authFactory.loginAndGetToken(
                port,
                "teste2@gmail.com",
                "password123"
        );

        RestAssured
                .given()
                .header("Authorization", "Bearer " + otherToken)
                .when()
                .get("/{installmentId}/pay", installment.getInstallmentId())
                .then()
                .statusCode(403);
    }

    @Test
    void shouldUpdateInstallmentWhenRequestIsValid() {

        factory.createExpense(
                supplier.getId(),
                "INV-001",
                BigDecimal.valueOf(300),
                LocalDate.now(),
                port,
                token
        );

        Expense expense = expenseRepository.findAll()
                .stream()
                .filter(ex -> ex.getInvoiceNumber().equals("INV-001"))
                .findFirst()
                .orElseThrow();

        Installment installment = installmentRepository.findAll()
                .stream()
                .filter(i -> i.getExpense().getId().equals(expense.getId()))
                .findFirst()
                .orElseThrow();

        InstallmentUpdateDto dto = new InstallmentUpdateDto(
                BigDecimal.valueOf(500),
                LocalDate.now().plusDays(10),
                "12345678901234567890"
        );

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(dto)
                .when()
                .put("/{installmentId}", installment.getInstallmentId())
                .then()
                .statusCode(200);

        Installment updated =
                installmentRepository.findById(installment.getInstallmentId())
                        .orElseThrow();

        assertEquals(500, updated.getAmount().doubleValue());
        assertEquals(LocalDate.now().plusDays(10), updated.getDueDate());
        assertEquals("12345678901234567890", updated.getBarcode());
    }

    @Test
    void shouldReturn400WhenRequestBodyIsInvalid() {

        factory.createExpense(
                supplier.getId(),
                "INV-001",
                BigDecimal.valueOf(300),
                LocalDate.now(),
                port,
                token
        );

        Installment installment = installmentRepository.findAll().get(0);

        InstallmentUpdateDto dto = new InstallmentUpdateDto(
                BigDecimal.valueOf(-500),
                LocalDate.now().plusDays(10),
                "12345678901234567890"
        );

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(dto)
                .when()
                .put("/{installmentId}", installment.getInstallmentId())
                .then()
                .statusCode(400);
    }

    @Test
    void shouldReturn404WhenInstallmentDoesNotExist() {

        InstallmentUpdateDto dto = new InstallmentUpdateDto(
                BigDecimal.valueOf(500),
                LocalDate.now().plusDays(10),
                "12345678901234567890"
        );

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(dto)
                .when()
                .put("/{installmentId}", UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn403WhenUpdatingInstallmentDoesNotBelongToUser() {

        User otherUser = userRepository.save(
                new User(
                        "Jane Doe",
                        null,
                        "testefs@gmail.com",
                        passwordEncoder.encode("password123"),
                        "12345678900",
                        PersonType.PF
                )
        );

        factory.createExpense(
                supplier.getId(),
                "INV-001",
                BigDecimal.valueOf(300),
                LocalDate.now(),
                port,
                token
        );

        Installment installment = installmentRepository.findAll().get(0);

        String otherToken = authFactory.loginAndGetToken(
                port,
                "testefs@gmail.com",
                "password123"
        );

        InstallmentUpdateDto dto = new InstallmentUpdateDto(
                BigDecimal.valueOf(500),
                LocalDate.now().plusDays(10),
                "12345678901234567890"
        );

        RestAssured
                .given()
                .header("Authorization", "Bearer " + otherToken)
                .contentType("application/json")
                .body(dto)
                .when()
                .put("/{installmentId}", installment.getInstallmentId())
                .then()
                .statusCode(403);
    }
}

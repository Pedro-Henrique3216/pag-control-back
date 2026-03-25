package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.InstallmentUpdateDto;
import com.pedrohenrique.pagcontrolback.helpers.AuthTestFactory;
import com.pedrohenrique.pagcontrolback.helpers.ExpenseFactory;
import com.pedrohenrique.pagcontrolback.helpers.SupplierFactory;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.model.Installment;
import com.pedrohenrique.pagcontrolback.model.InstallmentStatus;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
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
    private ExpenseRepository expenseRepository;

    @Autowired
    private InstallmentRepository installmentRepository;

    @Autowired
    private ExpenseFactory factory;

    @Autowired
    private AuthTestFactory authFactory;

    @Autowired
    private SupplierFactory supplierFactory;

    private UUID supplierId;

    private String token;

    @BeforeEach
    void setUp() {

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/installments";

        userRepository.deleteAll();

        authFactory.createUser(
                "teste",
                "teste@gmail.com",
                "Password123@",
                "11999999999",
                port
        );

        token = authFactory.loginAndGetToken(
                port,
                "teste@gmail.com",
                "Password123@"
        );

        supplierId = supplierFactory.createSupplier(
                "Supplier Inc.",
                null,
                port,
                token
        );
    }

    @Test
    void whenGetInstallmentsWithoutFilters_thenReturnAllUserInstallments() {

        factory.createExpense(supplierId, "INV-001", LocalDate.now(), port, token);
        factory.createExpense(supplierId, "INV-002", LocalDate.now(), port, token);

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
                supplierId,
                "INV-001",
                LocalDate.of(2026, 2, 1),
                port,
                token
        );

        factory.createExpense(
                supplierId,
                "INV-002",
                LocalDate.now(),
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

        UUID otherSupplier = supplierFactory.createSupplier(
                "Other Supplier Inc.",
                null,
                port,
                token
        );

        factory.createExpense(supplierId, "INV-001", LocalDate.now(), port, token);
        factory.createExpense(otherSupplier, "INV-002", LocalDate.now(), port, token);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("supplier_id", supplierId)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));
    }

    @Test
    void whenGetInstallmentsFilteredByStatus_thenReturnOnlyInstallmentsWithStatus() {

        factory.createExpense(
                supplierId,
                "INV-001",
                BigDecimal.valueOf(300),
                LocalDate.now(),
                port,
                token
        );

        factory.createExpense(
                supplierId,
                "INV-002",
                LocalDate.now(),
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
                supplierId,
                "INV-001",
                BigDecimal.valueOf(300),
                LocalDate.now().minusDays(20),
                port,
                token
        );

        factory.createExpense(
                supplierId,
                "INV-002",
                BigDecimal.valueOf(300),
                LocalDate.now(),
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
                supplierId,
                "INV-001",
                BigDecimal.valueOf(300),
                LocalDate.now().minusDays(10),
                port,
                token
        );

        factory.createExpense(
                supplierId,
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
                supplierId,
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

        authFactory.createUser(
                "Jane Doe",
                "teste2@gmail.com",
                "Password123@",
                "11912345678",
                port
        );

        factory.createExpense(
                supplierId,
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
                "Password123@"
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
                supplierId,
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
                supplierId,
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

        authFactory.createUser(
                "Jane Doe",
                "testefs@gmail.com",
                "Password123@",
                "11987654321",
                port
        );

        factory.createExpense(
                supplierId,
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
                "Password123@"
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

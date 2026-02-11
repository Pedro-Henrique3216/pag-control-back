package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.ExpenseRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.ExpenseResponseDto;
import com.pedrohenrique.pagcontrolback.helpers.TestDataFactory;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExpenseControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    private User user;
    private Supplier supplier;

    @BeforeEach
    void setUp() {

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/expenses";

        expenseRepository.deleteAll();
        supplierRepository.deleteAll();
        userRepository.deleteAll();

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
    void whenCreateExpenseWithInstallments_thenReturn201() {

        ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto(
                null,
                PaymentType.CREDIT,
                supplier.getId(),
                LocalDate.of(2026, 2, 2),
                new HashMap<>() {{
                    put(30, "1234567890123456");
                    put(60, "9876543210987654");
                }},
                BigDecimal.valueOf(400.00)
        );

        var response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(expenseRequestDto)
                .when()
                .post("/{userId}", user.getId())
                .then()
                .statusCode(201)
                .extract()
                .response();

        ExpenseResponseDto expenseResponseDto =
                response.body().as(ExpenseResponseDto.class);

        assertNotNull(response);
        assertEquals(200, expenseResponseDto.installments().get(0).amount().intValue());
        assertEquals(2, expenseResponseDto.installments().size());
        assertEquals(LocalDate.of(2026, 2, 2), expenseResponseDto.date());
        assertEquals("9876543210987654", expenseResponseDto.installments().get(1).barcode());
        assertEquals(InstallmentStatus.UNPAID, expenseResponseDto.installments().get(0).status());
    }

    @Test
    void whenRequestBodyIsInvalid_thenReturn400() {

        ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto(
                null,
                null,
                supplier.getId(),
                LocalDate.of(2026, 2, 2),
                null,
                null
        );

        var response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(expenseRequestDto)
                .when()
                .post("/{userId}", user.getId())
                .then()
                .statusCode(400)
                .extract()
                .response();

        List<String> errors = response.path("errors");

        assertNotNull(errors);
        assertTrue(errors.contains("Payment type is required"));
        assertTrue(errors.contains("Total amount is required"));
    }

    @Test
    void whenUserNotFound_thenReturn404() {

        ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto(
                null,
                PaymentType.CREDIT,
                supplier.getId(),
                LocalDate.of(2026, 2, 2),
                new HashMap<>() {{
                    put(30, "1234567890123456");
                    put(60, "9876543210987654");
                }},
                BigDecimal.valueOf(400.00)
        );

        UUID randomUserId = UUID.randomUUID();

        var response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(expenseRequestDto)
                .when()
                .post("/{userId}", randomUserId)
                .then()
                .statusCode(404)
                .extract()
                .response();

        List<String> errors = response.path("errors");

        assertNotNull(errors);
        assertTrue(errors.contains("User not found with id: " + randomUserId));
    }

    @Test
    void whenSupplierNotFound_thenReturn404() {

        UUID randomSupplierId = UUID.randomUUID();

        ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto(
                null,
                PaymentType.CREDIT,
                randomSupplierId,
                LocalDate.of(2026, 2, 2),
                new HashMap<>() {{
                    put(30, "1234567890123456");
                    put(60, "9876543210987654");
                }},
                BigDecimal.valueOf(400.00)
        );

        var response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(expenseRequestDto)
                .when()
                .post("/{userId}", user.getId())
                .then()
                .statusCode(404)
                .extract()
                .response();

        List<String> errors = response.path("errors");

        assertNotNull(errors);
        assertTrue(errors.contains("Supplier not found with id: " + randomSupplierId));
    }

    @Test
    void whenInstallmentsAreMissing_thenReturn400() {

        ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto(
                null,
                PaymentType.CREDIT,
                supplier.getId(),
                LocalDate.of(2026, 2, 2),
                null,
                BigDecimal.valueOf(400.00)
        );

        var response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(expenseRequestDto)
                .when()
                .post("/{userId}", user.getId())
                .then()
                .statusCode(400)
                .extract()
                .response();

        List<String> errors = response.path("errors");

        assertNotNull(errors);
        assertTrue(errors.contains(
                "Installment intervals must be provided for CREDIT or BILL payment types."
        ));
    }

    @Test
    void whenInstallmentDueInDaysIsInvalid_thenReturn400() {

        ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto(
                null,
                PaymentType.CREDIT,
                supplier.getId(),
                LocalDate.of(2026, 2, 2),
                new HashMap<>() {{
                    put(-1, "1234567890123456");
                }},
                BigDecimal.valueOf(400.00)
        );

        var response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(expenseRequestDto)
                .when()
                .post("/{userId}", user.getId())
                .then()
                .statusCode(400)
                .extract()
                .response();

        List<String> errors = response.path("errors");

        assertNotNull(errors);
        assertTrue(errors.contains("Installment due in days must be greater than zero."));
    }

    @Test
    void whenGetExpensesWithoutFilters_thenReturnAllUserExpenses() {

        testDataFactory.createExpense(user.getId(), supplier.getId(), "INV-1", port);
        testDataFactory.createExpense(user.getId(), supplier.getId(), "INV-2", port);

        var response =
                RestAssured.given()
                        .accept(ContentType.JSON)
                        .when()
                        .get("/{userId}", user.getId())
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        List<String> invoices = response.jsonPath().getList("invoice_number");

        assertEquals(2, invoices.size());
        assertTrue(invoices.contains("INV-1"));
        assertTrue(invoices.contains("INV-2"));
    }

    @Test
    void whenGetExpensesByInvoiceNumber_thenReturnOnlyMatchingExpense() {

        testDataFactory.createExpense(user.getId(), supplier.getId(), "INV-100", port);
        testDataFactory.createExpense(user.getId(), supplier.getId(), "INV-200", port);

        var response =
                RestAssured.given()
                        .accept(ContentType.JSON)
                        .queryParam("invoice_number", "INV-100")
                        .when()
                        .get("/{userId}", user.getId())
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        List<String> invoices =
                response.jsonPath().getList("invoice_number");

        assertEquals(1, invoices.size());
        assertEquals("INV-100", invoices.get(0));
    }

    @Test
    void whenGetExpensesBySupplier_thenReturnOnlySupplierExpenses() {

        Supplier otherSupplier = new Supplier("Other supplier");
        otherSupplier.setUser(user);
        otherSupplier = supplierRepository.save(otherSupplier);

        testDataFactory.createExpense(user.getId(), supplier.getId(), "INV-1", port);
        testDataFactory.createExpense(user.getId(), otherSupplier.getId(), "INV-2", port);

        var response =
                RestAssured.given()
                        .accept(ContentType.JSON)
                        .queryParam("supplier_id", supplier.getId())
                        .when()
                        .get("/{userId}", user.getId())
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        List<String> invoices =
                response.jsonPath().getList("invoiceNumber");

        assertEquals(1, invoices.size());
    }

    @Test
    void whenGetExpensesByMonth_thenReturnOnlyExpensesInThatMonth() {

        testDataFactory.createExpense(user.getId(), supplier.getId(), "INV-JAN", LocalDate.of(2026, 1, 10), port);
        testDataFactory.createExpense(user.getId(), supplier.getId(), "INV-FEB", LocalDate.of(2026, 2, 9), port);

        var response =
                RestAssured.given()
                        .accept(ContentType.JSON)
                        .queryParam("month", "2026-02")
                        .when()
                        .get("/{userId}", user.getId())
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        List<String> invoices =
                response.jsonPath().getList("invoice_number");

        assertEquals(1, invoices.size());
        assertEquals("INV-FEB", invoices.get(0));
    }

    @Test
    void whenGetExpensesForUserWithoutExpenses_thenReturnEmptyList() {

        var response =
                RestAssured.given()
                        .accept(ContentType.JSON)
                        .when()
                        .get("/{userId}", user.getId())
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        List<?> list = response.jsonPath().getList("$");

        assertTrue(list.isEmpty());
    }

    @Test
    void whenGetExpensesWithFutureMonth_thenReturn400() {

        YearMonth future = YearMonth.now().plusMonths(1);

        var response =
                RestAssured.given()
                        .accept(ContentType.JSON)
                        .queryParam("month", future.toString())
                        .when()
                        .get("/{userId}", user.getId())
                        .then()
                        .statusCode(400)
                        .extract()
                        .response();

        List<String> errors = response.path("errors");

        assertNotNull(errors);
        assertTrue(errors.contains("Month cannot be in the future."));
    }
}

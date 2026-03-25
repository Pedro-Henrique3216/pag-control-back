package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.ExpenseRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.ExpenseResponseDto;
import com.pedrohenrique.pagcontrolback.helpers.AuthTestFactory;
import com.pedrohenrique.pagcontrolback.helpers.CategoryFactory;
import com.pedrohenrique.pagcontrolback.helpers.ExpenseFactory;
import com.pedrohenrique.pagcontrolback.helpers.SupplierFactory;
import com.pedrohenrique.pagcontrolback.model.InstallmentStatus;
import com.pedrohenrique.pagcontrolback.model.PaymentType;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ExpenseControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseFactory expenseFactory;

    @Autowired
    private AuthTestFactory authTestFactory;

    @Autowired
    private SupplierFactory supplierFactory;

    @Autowired
    private CategoryFactory categoryFactory;

    private String token;

    private UUID supplierId;

    @BeforeEach
    void setUp() {

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/expenses";

        userRepository.deleteAll();

        authTestFactory.createUser(
                "teste",
                "teste@gmail.com",
                "Password123@",
                "11999999999",
                port
        );

        token = authTestFactory.loginAndGetToken(
                port,
                "teste@gmail.com",
                "Password123@"
        );

        supplierId = supplierFactory.createSupplier(
                "teste",
                null,
                port,
                token
        );

    }

    @Test
    void whenCreateExpenseWithInstallments_thenReturn201() {

        UUID categoryId = categoryFactory.createCategory(port, token);

        ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto(
                null,
                PaymentType.CREDIT,
                supplierId,
                LocalDate.of(2026, 2, 2),
                new HashMap<>() {{
                    put(30, "1234567890123456");
                    put(60, "9876543210987654");
                }},
                BigDecimal.valueOf(400.00),
                categoryId
        );

        var response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(expenseRequestDto)
                .when()
                .post()
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
        assertEquals(categoryId, expenseResponseDto.categoryId());
    }

    @Test
    void whenRequestBodyIsInvalid_thenReturn400() {

        ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto(
                null,
                null,
                supplierId,
                LocalDate.of(2026, 2, 2),
                null,
                null,
                null
        );

        var response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(expenseRequestDto)
                .when()
                .post()
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
                BigDecimal.valueOf(400.00),
                null
        );

        var response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(expenseRequestDto)
                .when()
                .post()
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
                supplierId,
                LocalDate.of(2026, 2, 2),
                null,
                BigDecimal.valueOf(400.00),
                null
        );

        var response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(expenseRequestDto)
                .when()
                .post()
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
                supplierId,
                LocalDate.of(2026, 2, 2),
                new HashMap<>() {{
                    put(-1, "1234567890123456");
                }},
                BigDecimal.valueOf(400.00),
                null
        );

        var response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(expenseRequestDto)
                .when()
                .post()
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

        expenseFactory.createExpense(supplierId, "INV-1", LocalDate.now(), port, token);
        expenseFactory.createExpense(supplierId, "INV-2", LocalDate.now(), port, token);

        var response =
                RestAssured.given()
                        .accept(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get()
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

        expenseFactory.createExpense(supplierId, "INV-100", LocalDate.now(), port, token);

        expenseFactory.createExpense(supplierId, "INV-200", LocalDate.now(), port, token);

        var response =
                RestAssured.given()
                        .accept(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .queryParam("invoice_number", "INV-100")
                        .when()
                        .get()
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

        UUID otherSupplierId = supplierFactory.createSupplier(
                "other supplier",
                null,
                port,
                token
        );

        expenseFactory.createExpense(supplierId, "INV-1", LocalDate.now(), port, token);
        expenseFactory.createExpense(otherSupplierId, "INV-2", LocalDate.now(), port, token);

        var response =
                RestAssured.given()
                        .accept(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .queryParam("supplier_id", supplierId)
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        List<String> invoices =
                response.jsonPath().getList("invoice_number");

        assertEquals(1, invoices.size());
    }

    @Test
    void whenGetExpensesByMonth_thenReturnOnlyExpensesInThatMonth() {

        expenseFactory.createExpense(
                supplierId,
                "INV-JAN",
                LocalDate.of(2026, 1, 10),
                port,
                token
        );

        expenseFactory.createExpense(
                supplierId,
                "INV-FEB",
                LocalDate.of(2026, 2, 10),
                port,
                token
        );

        var response =
                RestAssured.given()
                        .accept(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .queryParam("month", "2026-02")
                        .when()
                        .get()
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
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get()
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
                        .header("Authorization", "Bearer " + token)
                        .queryParam("month", future.toString())
                        .when()
                        .get()
                        .then()
                        .statusCode(400)
                        .extract()
                        .response();

        List<String> errors = response.path("errors");

        assertNotNull(errors);
        assertTrue(errors.contains("Month cannot be in the future."));
    }
}

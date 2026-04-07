package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.helpers.*;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DashboardControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AuthTestFactory authTestFactory;

    @Autowired
    private ExpenseFactory expenseFactory;

    @Autowired
    private SupplierFactory supplierFactory;

    @Autowired
    private InstallmentHelper installmentHelper;

    @Autowired
    private IncomeFactory incomeFactory;

    @Autowired
    private CategoryFactory categoryFactory;

    @Autowired
    private UserRepository userRepository;

    private String token;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/dashboard";

        userRepository.deleteAll();

        authTestFactory.createUser(
                "teste",
                "teste@gmail.com",
                "Password123@",
                "11912345678",
                port
        );

        token = authTestFactory.loginAndGetToken(
                port,
                "teste@gmail.com",
                "Password123@"
        );
    }

    @Nested
    class Success {

        @Test
        void shouldReturnDashboardData() {

            UUID categoryId = categoryFactory.createCategoryExpense(port, token);

            UUID supplierId = supplierFactory.createSupplier(
                    "Fornecedor Teste",
                    null,
                    port,
                    token
            );

            expenseFactory.createExpense(
                    supplierId,
                    "teste",
                    BigDecimal.valueOf(1000),
                    LocalDate.of(2026, 2, 15),
                    categoryId,
                    port,
                    token
            );

            expenseFactory.createExpense(
                    supplierId,
                    "outros",
                    BigDecimal.valueOf(1250),
                    LocalDate.of(2026, 2, 15),
                    port,
                    token
            );

            incomeFactory.createIncome(
                    BigDecimal.valueOf(3000),
                    "teste",
                    LocalDate.of(2026, 2, 20),
                    port,
                    token
            );

            List<UUID> installmentIds = installmentHelper.getInstallments(token, port);
            payInstallments(installmentIds);

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .queryParam("month", "2026-02")
                    .when()
                    .get()
                    .then()
                    .statusCode(200)
                    .body("total_income", equalTo(3000.0F))
                    .body("total_expense", equalTo(2250.0F))
                    .body("balance", equalTo(750.0F))
                    .body("overdue_total", equalTo(0))
                    .body("overdue_count", equalTo(0))
                    .body("expenses_by_category.size()", equalTo(2));
        }

        @Test
        void shouldReturnOverdueData() {

            UUID supplierId = supplierFactory.createSupplier(
                    "Fornecedor",
                    null,
                    port,
                    token
            );

            expenseFactory.createExpense(
                    supplierId,
                    "teste",
                    BigDecimal.valueOf(1000),
                    LocalDate.now().minusDays(10),
                    port,
                    token
            );

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .queryParam("month", LocalDate.now().getYear() + "-" + String.format("%02d", LocalDate.now().getMonthValue()))
                    .when()
                    .get()
                    .then()
                    .statusCode(200)
                    .body("overdue_total", greaterThan(0.0F))
                    .body("overdue_count", greaterThan(0));
        }

        @Test
        void shouldReturnZeroWhenNoData() {

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .queryParam("month", "2026-02")
                    .when()
                    .get()
                    .then()
                    .statusCode(200)
                    .body("total_income", equalTo(0))
                    .body("total_expense", equalTo(0))
                    .body("balance", equalTo(0))
                    .body("overdue_total", equalTo(0))
                    .body("overdue_count", equalTo(0));
        }

        @Test
        void shouldIgnoreUnpaidExpenses() {

            UUID supplierId = supplierFactory.createSupplier(
                    "Fornecedor",
                    null,
                    port,
                    token
            );

            expenseFactory.createExpense(
                    supplierId,
                    "teste",
                    BigDecimal.valueOf(1000),
                    LocalDate.of(2026, 2, 10),
                    port,
                    token
            );

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .queryParam("month", "2026-02")
                    .when()
                    .get()
                    .then()
                    .statusCode(200)
                    .body("total_expense", equalTo(0));
        }

        @Test
        void shouldIgnoreDataFromOtherMonths() {

            UUID supplierId = supplierFactory.createSupplier(
                    "Fornecedor",
                    null,
                    port,
                    token
            );

            expenseFactory.createExpense(
                    supplierId,
                    "fev",
                    BigDecimal.valueOf(1000),
                    LocalDate.of(2026, 2, 10),
                    port,
                    token
            );

            expenseFactory.createExpense(
                    supplierId,
                    "jan",
                    BigDecimal.valueOf(2000),
                    LocalDate.of(2026, 1, 10),
                    port,
                    token
            );

            List<UUID> ids = installmentHelper.getInstallments(token, port);
            payInstallments(ids);

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .queryParam("month", "2026-02")
                    .when()
                    .get()
                    .then()
                    .statusCode(200)
                    .body("total_expense", equalTo(1000.0F));
        }

        @Test
        void shouldReturnNegativeBalance() {

            UUID supplierId = supplierFactory.createSupplier(
                    "Fornecedor",
                    null,
                    port,
                    token
            );

            expenseFactory.createExpense(
                    supplierId,
                    "teste",
                    BigDecimal.valueOf(2000),
                    LocalDate.of(2026, 2, 10),
                    port,
                    token
            );

            List<UUID> ids = installmentHelper.getInstallments(token, port);
            payInstallments(ids);

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .queryParam("month", "2026-02")
                    .when()
                    .get()
                    .then()
                    .statusCode(200)
                    .body("balance", equalTo(-2000.0F));
        }
    }

    @Nested
    class Errors {

        @Test
        void shouldReturn400WhenMonthIsInvalid() {

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .queryParam("month", "invalid")
                    .when()
                    .get()
                    .then()
                    .statusCode(400);
        }

        @Test
        void shouldReturn403WhenNoToken() {

            RestAssured.given()
                    .queryParam("month", "2026-02")
                    .when()
                    .get()
                    .then()
                    .statusCode(403);
        }
    }

    private void payInstallments(List<UUID> installmentIds) {
        installmentIds.forEach(id ->
                installmentHelper.payInstallment(id, token, port)
        );
    }
}
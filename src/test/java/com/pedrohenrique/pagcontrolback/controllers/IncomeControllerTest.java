package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.IncomeRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.request.IncomeUpdateDto;
import com.pedrohenrique.pagcontrolback.helpers.AuthTestFactory;
import com.pedrohenrique.pagcontrolback.helpers.CategoryFactory;
import com.pedrohenrique.pagcontrolback.helpers.DatabaseCleaner;
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
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class IncomeControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner cleaner;

    @Autowired
    private AuthTestFactory authTestFactory;

    @Autowired
    private CategoryFactory categoryFactory;

    private String authToken;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/incomes";

        cleaner.clearDatabase();

        authTestFactory.createUser(
                "teste",
                "teste@gmail.com",
                "Password123@",
                "11912345678"
        );

        authToken = authTestFactory.loginAndGetToken(
                port,
                "teste@gmail.com",
                "Password123@"
        );

        categoryId = categoryFactory.createCategoryIncome(
                port,
                authToken
        );
    }

    private UUID createIncome() {
        IncomeRequestDto body = new IncomeRequestDto(
                BigDecimal.valueOf(1000.00),
                "Salary",
                LocalDate.of(2024, 3, 10),
                categoryId
        );

        String id = RestAssured
                .given()
                .header("Authorization", "Bearer " + authToken)
                .contentType("application/json")
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        return UUID.fromString(id);
    }

    @Nested
    class CreateIncome {

        @Nested
        class Success {

            @Test
            void shouldCreateIncomeSuccessfully() {
                IncomeRequestDto body = new IncomeRequestDto(
                        BigDecimal.valueOf(1000.00),
                        "Salary",
                        LocalDate.of(2024, 3, 10),
                        categoryId
                );

                RestAssured
                        .given()
                        .header("Authorization", "Bearer " + authToken)
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post()
                        .then()
                        .statusCode(201)
                        .body("id", notNullValue())
                        .body("amount", equalTo(1000.00F))
                        .body("description", equalTo("Salary"))
                        .body("category.category_id", equalTo(categoryId.toString()))
                        .body("category.category_name", equalTo("teste"))
                        .body("date", equalTo("2024-03-10"));
            }
        }

        @Nested
        class Errors {

            @Test
            void shouldReturn400WhenAmountIsNull() {
                IncomeRequestDto body = new IncomeRequestDto(
                        null,
                        "Salary",
                        LocalDate.of(2024, 3, 10),
                        categoryId
                );

                RestAssured
                        .given()
                        .header("Authorization", "Bearer " + authToken)
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post()
                        .then()
                        .statusCode(400);
            }

            @Test
            void shouldReturn404WhenCategoryNotFound() {
                IncomeRequestDto body = new IncomeRequestDto(
                        BigDecimal.valueOf(1000.00),
                        "Salary",
                        LocalDate.of(2024, 3, 10),
                        UUID.randomUUID()
                );

                RestAssured
                        .given()
                        .header("Authorization", "Bearer " + authToken)
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post()
                        .then()
                        .statusCode(404);
            }

            @Test
            void shouldReturn401WhenTokenIsInvalid() {
                IncomeRequestDto body = new IncomeRequestDto(
                        BigDecimal.valueOf(1000.00),
                        "Salary",
                        LocalDate.of(2024, 3, 10),
                        categoryId
                );

                RestAssured
                        .given()
                        .header("Authorization", "Bearer invalid-token")
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post()
                        .then()
                        .statusCode(401);
            }
        }
    }

    @Nested
    class ListIncomes {

        @Test
        void shouldListAllActiveIncomes() {
            createIncome();
            createIncome();

            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .when()
                    .get()
                    .then()
                    .statusCode(200)
                    .body("size()", equalTo(2));
        }

        @Test
        void shouldReturnEmptyListWhenThereAreNoIncomes() {
            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .when()
                    .get()
                    .then()
                    .statusCode(200)
                    .body("size()", equalTo(0));
        }

        @Test
        void shouldFilterIncomesByDate() {
            createIncome();

            IncomeRequestDto anotherIncome = new IncomeRequestDto(
                    BigDecimal.valueOf(500.00),
                    "Freelance",
                    LocalDate.of(2024, 4, 15),
                    categoryId
            );

            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .contentType("application/json")
                    .body(anotherIncome)
                    .when()
                    .post()
                    .then()
                    .statusCode(201);

            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .queryParam("date", "2024-03-10")
                    .when()
                    .get()
                    .then()
                    .statusCode(200)
                    .body("size()", equalTo(1))
                    .body("[0].description", equalTo("Salary"));
        }

        @Test
        void shouldNotReturnDeactivatedIncomes() {
            UUID incomeId = createIncome();

            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .when()
                    .delete("/{incomeId}", incomeId)
                    .then()
                    .statusCode(204);

            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .when()
                    .get()
                    .then()
                    .statusCode(200)
                    .body("size()", equalTo(0));
        }

        @Test
        void shouldReturn401WhenTokenIsInvalid() {
            RestAssured
                    .given()
                    .header("Authorization", "Bearer invalid-token")
                    .when()
                    .get()
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    class GetIncomeById {

        @Test
        void shouldGetIncomeByIdSuccessfully() {
            UUID incomeId = createIncome();

            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .when()
                    .get("/{incomeId}", incomeId)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(incomeId.toString()))
                    .body("amount", equalTo(1000.00F))
                    .body("description", equalTo("Salary"))
                    .body("category.category_id", equalTo(categoryId.toString()))
                    .body("date", equalTo("2024-03-10"));
        }

        @Test
        void shouldReturn404WhenIncomeDoesNotExist() {
            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .when()
                    .get("/{incomeId}", UUID.randomUUID())
                    .then()
                    .statusCode(404);
        }

        @Test
        void shouldReturn404WhenIncomeIsDeactivated() {
            UUID incomeId = createIncome();

            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .when()
                    .delete("/{incomeId}", incomeId)
                    .then()
                    .statusCode(204);

            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .when()
                    .get("/{incomeId}", incomeId)
                    .then()
                    .statusCode(404);
        }

        @Test
        void shouldReturn401WhenTokenIsInvalid() {
            RestAssured
                    .given()
                    .header("Authorization", "Bearer invalid-token")
                    .when()
                    .get("/{incomeId}", UUID.randomUUID())
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    class UpdateIncome {

        @Test
        void shouldUpdateIncomeSuccessfully() {
            UUID incomeId = createIncome();

            IncomeUpdateDto body = new IncomeUpdateDto(
                    BigDecimal.valueOf(2000.00),
                    "Updated salary",
                    LocalDate.of(2024, 5, 20),
                    categoryId
            );

            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .contentType("application/json")
                    .body(body)
                    .when()
                    .put("/{incomeId}", incomeId)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(incomeId.toString()))
                    .body("amount", equalTo(2000.00F))
                    .body("description", equalTo("Updated salary"))
                    .body("date", equalTo("2024-05-20"))
                    .body("category.category_id", equalTo(categoryId.toString()));
        }

        @Test
        void shouldReturn404WhenIncomeDoesNotExist() {
            IncomeUpdateDto body = new IncomeUpdateDto(
                    BigDecimal.valueOf(2000.00),
                    "Updated salary",
                    LocalDate.of(2024, 5, 20),
                    categoryId
            );

            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .contentType("application/json")
                    .body(body)
                    .when()
                    .put("/{incomeId}", UUID.randomUUID())
                    .then()
                    .statusCode(404);
        }

        @Test
        void shouldReturn404WhenUpdatingDeactivatedIncome() {
            UUID incomeId = createIncome();

            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .when()
                    .delete("/{incomeId}", incomeId)
                    .then()
                    .statusCode(204);

            IncomeUpdateDto body = new IncomeUpdateDto(
                    BigDecimal.valueOf(2000.00),
                    "Updated salary",
                    LocalDate.of(2024, 5, 20),
                    categoryId
            );

            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .contentType("application/json")
                    .body(body)
                    .when()
                    .put("/{incomeId}", incomeId)
                    .then()
                    .statusCode(404);
        }

        @Test
        void shouldReturn400WhenAmountIsInvalid() {
            UUID incomeId = createIncome();

            IncomeUpdateDto body = new IncomeUpdateDto(
                    BigDecimal.valueOf(-100.00),
                    "Updated salary",
                    LocalDate.of(2024, 5, 20),
                    categoryId
            );

            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .contentType("application/json")
                    .body(body)
                    .when()
                    .put("/{incomeId}", incomeId)
                    .then()
                    .statusCode(400);
        }

        @Test
        void shouldReturn401WhenTokenIsInvalid() {
            IncomeUpdateDto body = new IncomeUpdateDto(
                    BigDecimal.valueOf(2000.00),
                    "Updated salary",
                    LocalDate.of(2024, 5, 20),
                    categoryId
            );

            RestAssured
                    .given()
                    .header("Authorization", "Bearer invalid-token")
                    .contentType("application/json")
                    .body(body)
                    .when()
                    .put("/{incomeId}", UUID.randomUUID())
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    class DeleteIncome {

        @Test
        void shouldDeleteIncomeSuccessfully() {
            UUID incomeId = createIncome();

            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .when()
                    .delete("/{incomeId}", incomeId)
                    .then()
                    .statusCode(204);
        }

        @Test
        void shouldReturn404WhenIncomeDoesNotExist() {
            RestAssured
                    .given()
                    .header("Authorization", "Bearer " + authToken)
                    .when()
                    .delete("/{incomeId}", UUID.randomUUID())
                    .then()
                    .statusCode(404);
        }

        @Test
        void shouldReturn401WhenTokenIsInvalid() {
            RestAssured
                    .given()
                    .header("Authorization", "Bearer invalid-token")
                    .when()
                    .delete("/{incomeId}", UUID.randomUUID())
                    .then()
                    .statusCode(401);
        }
    }
}

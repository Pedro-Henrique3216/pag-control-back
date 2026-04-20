package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.IncomeRequestDto;
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
                "11912345678",
                port
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
                        .body("category_id", equalTo(categoryId.toString()))
                        .body("date", equalTo(LocalDate.of(2024, 3, 10).toString()));
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
                        UUID.randomUUID()
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
}
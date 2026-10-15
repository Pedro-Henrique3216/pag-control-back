package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.RecurringExpenseRequestDto;
import com.pedrohenrique.pagcontrolback.helpers.AuthTestFactory;
import com.pedrohenrique.pagcontrolback.helpers.DatabaseCleaner;
import com.pedrohenrique.pagcontrolback.model.PaymentType;
import com.pedrohenrique.pagcontrolback.model.RecurrenceType;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class RecurringExpenseControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AuthTestFactory authTestFactory;

    @Autowired
    private DatabaseCleaner cleaner;

    private String token;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/recurring-expenses";

        cleaner.clearDatabase();

        authTestFactory.createUser(
                "John Doe",
                "teste@gmail.com",
                "Password123@",
                "11912345678"
        );

        token = authTestFactory.loginAndGetToken(
                port,
                "teste@gmail.com",
                "Password123@"
        );
    }

    @Nested
    class CreateRecurringExpense {

        @Nested
        class Success {

            @Test
            void shouldCreateRecurringExpenseSuccessfully() {

                RecurringExpenseRequestDto body =
                        new RecurringExpenseRequestDto(
                                "Internet",
                                PaymentType.PIX,
                                null,
                                LocalDate.of(2026, 10, 15),
                                null,
                                new BigDecimal("120.00"),
                                null,
                                15,
                                RecurrenceType.MONTHLY,
                                1,
                                null
                        );

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(body)
                        .when()
                        .post()
                        .then()
                        .statusCode(201)
                        .header("Location", notNullValue())
                        .body("id", notNullValue())
                        .body("description", equalTo("Internet"))
                        .body("payment_type", equalTo("PIX"))
                        .body("start_date", equalTo("2026-10-15"))
                        .body("total_amount", comparesEqualTo(120.00f))
                        .body("category", nullValue())
                        .body("supplier", nullValue())
                        .body("credit_card", nullValue())
                        .body("recurrence_day", equalTo(15))
                        .body("recurrence_type", equalTo("MONTHLY"))
                        .body("recurrence_interval", equalTo(1))
                        .body("recurrence_end_date", nullValue());
            }
        }

        @Nested
        class Errors {

            @Test
            void shouldReturn400WhenDescriptionIsEmpty() {

                RecurringExpenseRequestDto body =
                        new RecurringExpenseRequestDto(
                                "",
                                PaymentType.PIX,
                                null,
                                LocalDate.of(2026, 10, 15),
                                null,
                                new BigDecimal("120.00"),
                                null,
                                15,
                                RecurrenceType.MONTHLY,
                                1,
                                null
                        );

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(body)
                        .when()
                        .post()
                        .then()
                        .statusCode(400);
            }

            @Test
            void shouldReturn400WhenPaymentTypeIsMissing() {

                RecurringExpenseRequestDto body =
                        new RecurringExpenseRequestDto(
                                "Internet",
                                null,
                                null,
                                LocalDate.of(2026, 10, 15),
                                null,
                                new BigDecimal("120.00"),
                                null,
                                15,
                                RecurrenceType.MONTHLY,
                                1,
                                null
                        );

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(body)
                        .when()
                        .post()
                        .then()
                        .statusCode(400);
            }

            @Test
            void shouldReturn400WhenTotalAmountIsZero() {

                RecurringExpenseRequestDto body =
                        new RecurringExpenseRequestDto(
                                "Internet",
                                PaymentType.PIX,
                                null,
                                LocalDate.of(2026, 10, 15),
                                null,
                                BigDecimal.ZERO,
                                null,
                                15,
                                RecurrenceType.MONTHLY,
                                1,
                                null
                        );

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(body)
                        .when()
                        .post()
                        .then()
                        .statusCode(400);
            }

            @Test
            void shouldReturn401WhenUserIsNotAuthenticated() {

                RecurringExpenseRequestDto body =
                        new RecurringExpenseRequestDto(
                                "Internet",
                                PaymentType.PIX,
                                null,
                                LocalDate.of(2026, 10, 15),
                                null,
                                new BigDecimal("120.00"),
                                null,
                                15,
                                RecurrenceType.MONTHLY,
                                1,
                                null
                        );

                given()
                        .contentType(ContentType.JSON)
                        .body(body)
                        .when()
                        .post()
                        .then()
                        .statusCode(401);
            }
        }
    }
}
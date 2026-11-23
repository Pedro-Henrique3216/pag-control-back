
package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.RecurringExpenseRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.request.RecurringExpenseUpdateDto;
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
import java.util.UUID;

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

    private final LocalDate startDate = LocalDate.of(2025, 10, 15);

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

    private RecurringExpenseRequestDto createRequest(
            String description,
            PaymentType paymentType,
            LocalDate startDate,
            BigDecimal totalAmount,
            Integer recurrenceDay,
            RecurrenceType recurrenceType,
            Integer recurrenceInterval,
            LocalDate recurrenceEndDate
    ) {
        return new RecurringExpenseRequestDto(
                description,
                paymentType,
                null,
                startDate,
                null,
                totalAmount,
                null,
                recurrenceDay,
                recurrenceType,
                recurrenceInterval,
                recurrenceEndDate
        );
    }

    private RecurringExpenseRequestDto validRequest() {
        return createRequest(
                "Internet",
                PaymentType.PIX,
                startDate,
                new BigDecimal("120.00"),
                15,
                RecurrenceType.MONTHLY,
                1,
                null
        );
    }

    private RecurringExpenseUpdateDto validUpdateRequest() {
        return new RecurringExpenseUpdateDto(
                "Internet updated",
                new BigDecimal("150.00"),
                PaymentType.PIX,
                null,
                null,
                null
        );
    }

    private String createRecurringExpense() {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(validRequest())
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    private String createAnotherUserAndGetToken() {
        authTestFactory.createUser(
                "Jane Doe",
                "jane@gmail.com",
                "Password123@",
                "11987654321"
        );

        return authTestFactory.loginAndGetToken(
                port,
                "jane@gmail.com",
                "Password123@"
        );
    }

    @Nested
    class CreateRecurringExpense {

        @Nested
        class Success {

            @Test
            void shouldCreateRecurringExpenseSuccessfully() {

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(validRequest())
                        .when()
                        .post()
                        .then()
                        .statusCode(201)
                        .header("Location", notNullValue())
                        .body("id", notNullValue())
                        .body("description", equalTo("Internet"))
                        .body("payment_type", equalTo("PIX"))
                        .body("start_date", equalTo("2025-10-15"))
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

                RecurringExpenseRequestDto body = createRequest(
                        "",
                        PaymentType.PIX,
                        startDate,
                        new BigDecimal("120.00"),
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
            void shouldReturn400WhenDescriptionContainsOnlySpaces() {

                RecurringExpenseRequestDto body = createRequest(
                        "   ",
                        PaymentType.PIX,
                        startDate,
                        new BigDecimal("120.00"),
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

                RecurringExpenseRequestDto body = createRequest(
                        "Internet",
                        null,
                        startDate,
                        new BigDecimal("120.00"),
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
            void shouldReturn400WhenStartDateIsMissing() {

                RecurringExpenseRequestDto body = createRequest(
                        "Internet",
                        PaymentType.PIX,
                        null,
                        new BigDecimal("120.00"),
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
            void shouldReturn400WhenTotalAmountIsMissing() {

                RecurringExpenseRequestDto body = createRequest(
                        "Internet",
                        PaymentType.PIX,
                        startDate,
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

                RecurringExpenseRequestDto body = createRequest(
                        "Internet",
                        PaymentType.PIX,
                        startDate,
                        BigDecimal.ZERO,
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
            void shouldReturn400WhenTotalAmountIsNegative() {

                RecurringExpenseRequestDto body = createRequest(
                        "Internet",
                        PaymentType.PIX,
                        startDate,
                        new BigDecimal("-10.00"),
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
            void shouldReturn400WhenRecurrenceTypeIsMissing() {

                RecurringExpenseRequestDto body = createRequest(
                        "Internet",
                        PaymentType.PIX,
                        startDate,
                        new BigDecimal("120.00"),
                        15,
                        null,
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
            void shouldReturn400WhenRecurrenceIntervalIsMissing() {

                RecurringExpenseRequestDto body = createRequest(
                        "Internet",
                        PaymentType.PIX,
                        startDate,
                        new BigDecimal("120.00"),
                        15,
                        RecurrenceType.MONTHLY,
                        null,
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
            void shouldReturn400WhenRecurrenceIntervalIsZero() {

                RecurringExpenseRequestDto body = createRequest(
                        "Internet",
                        PaymentType.PIX,
                        startDate,
                        new BigDecimal("120.00"),
                        15,
                        RecurrenceType.MONTHLY,
                        0,
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
            void shouldReturn400WhenRecurrenceDayIsZero() {

                RecurringExpenseRequestDto body = createRequest(
                        "Internet",
                        PaymentType.PIX,
                        startDate,
                        new BigDecimal("120.00"),
                        0,
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
            void shouldReturn400WhenRecurrenceDayIsGreaterThan31() {

                RecurringExpenseRequestDto body = createRequest(
                        "Internet",
                        PaymentType.PIX,
                        startDate,
                        new BigDecimal("120.00"),
                        32,
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
            void shouldReturn400WhenUserIsNotAuthenticated() {

                given()
                        .contentType(ContentType.JSON)
                        .body(validRequest())
                        .when()
                        .post()
                        .then()
                        .statusCode(401);
            }
        }
    }

    @Nested
    class ListRecurringExpenses {

        @Test
        void shouldReturnEmptyListWhenUserHasNoRecurringExpenses() {

            given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get()
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(0));
        }

        @Test
        void shouldReturnRecurringExpensesSuccessfully() {

            createRecurringExpense();

            given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get()
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(1))
                    .body("[0].description", equalTo("Internet"))
                    .body("[0].payment_type", equalTo("PIX"));
        }

        @Test
        void shouldReturnOnlyAuthenticatedUserRecurringExpenses() {

            createRecurringExpense();

            String anotherToken = createAnotherUserAndGetToken();

            given()
                    .header("Authorization", "Bearer " + anotherToken)
                    .when()
                    .get()
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(0));
        }

        @Test
        void shouldReturn401WhenUserIsNotAuthenticated() {

            given()
                    .when()
                    .get()
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    class FindRecurringExpenseById {

        @Test
        void shouldFindRecurringExpenseSuccessfully() {

            String id = createRecurringExpense();

            given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/{id}", id)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(id))
                    .body("description", equalTo("Internet"))
                    .body("payment_type", equalTo("PIX"))
                    .body("total_amount", comparesEqualTo(120.00f));
        }

        @Test
        void shouldReturn404WhenRecurringExpenseDoesNotExist() {

            String id = UUID.randomUUID().toString();

            given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/{id}", id)
                    .then()
                    .statusCode(404);
        }

        @Test
        void shouldReturn404WhenRecurringExpenseBelongsToAnotherUser() {

            String id = createRecurringExpense();

            String anotherToken = createAnotherUserAndGetToken();

            given()
                    .header("Authorization", "Bearer " + anotherToken)
                    .when()
                    .get("/{id}", id)
                    .then()
                    .statusCode(404);
        }

        @Test
        void shouldReturn401WhenUserIsNotAuthenticated() {

            String id = UUID.randomUUID().toString();

            given()
                    .when()
                    .get("/{id}", id)
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    class UpdateRecurringExpense {

        @Nested
        class Success {

            @Test
            void shouldUpdateRecurringExpenseSuccessfully() {

                String id = createRecurringExpense();

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(validUpdateRequest())
                        .when()
                        .put("/{id}", id)
                        .then()
                        .statusCode(200)
                        .body("id", equalTo(id))
                        .body("description", equalTo("Internet updated"))
                        .body("payment_type", equalTo("PIX"))
                        .body("total_amount", comparesEqualTo(150.00f))
                        .body("supplier", nullValue())
                        .body("category", nullValue())
                        .body("credit_card", nullValue());
            }

            @Test
            void shouldUpdateRecurringExpenseAndKeepOptionalAssociationsNull() {

                String id = createRecurringExpense();

                RecurringExpenseUpdateDto body = new RecurringExpenseUpdateDto(
                        "Updated expense",
                        new BigDecimal("200.00"),
                        PaymentType.DEBIT,
                        null,
                        null,
                        null
                );

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(body)
                        .when()
                        .put("/{id}", id)
                        .then()
                        .statusCode(200)
                        .body("description", equalTo("Updated expense"))
                        .body("payment_type", equalTo("DEBIT"))
                        .body("total_amount", comparesEqualTo(200.00f))
                        .body("supplier", nullValue())
                        .body("category", nullValue())
                        .body("credit_card", nullValue());
            }
        }

        @Nested
        class Errors {

            @Test
            void shouldReturn404WhenRecurringExpenseDoesNotExist() {

                String id = UUID.randomUUID().toString();

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(validUpdateRequest())
                        .when()
                        .put("/{id}", id)
                        .then()
                        .statusCode(404);
            }

            @Test
            void shouldReturn404WhenRecurringExpenseBelongsToAnotherUser() {

                String id = createRecurringExpense();

                String anotherToken = createAnotherUserAndGetToken();

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + anotherToken)
                        .body(validUpdateRequest())
                        .when()
                        .put("/{id}", id)
                        .then()
                        .statusCode(404);
            }

            @Test
            void shouldReturn400WhenDescriptionIsEmpty() {

                String id = createRecurringExpense();

                RecurringExpenseUpdateDto body = new RecurringExpenseUpdateDto(
                        "",
                        new BigDecimal("150.00"),
                        PaymentType.PIX,
                        null,
                        null,
                        null
                );

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(body)
                        .when()
                        .put("/{id}", id)
                        .then()
                        .statusCode(400);
            }

            @Test
            void shouldReturn400WhenTotalAmountIsZero() {

                String id = createRecurringExpense();

                RecurringExpenseUpdateDto body = new RecurringExpenseUpdateDto(
                        "Internet",
                        BigDecimal.ZERO,
                        PaymentType.PIX,
                        null,
                        null,
                        null
                );

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(body)
                        .when()
                        .put("/{id}", id)
                        .then()
                        .statusCode(400);
            }

            @Test
            void shouldReturn400WhenPaymentTypeIsMissing() {

                String id = createRecurringExpense();

                RecurringExpenseUpdateDto body = new RecurringExpenseUpdateDto(
                        "Internet",
                        new BigDecimal("150.00"),
                        null,
                        null,
                        null,
                        null
                );

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(body)
                        .when()
                        .put("/{id}", id)
                        .then()
                        .statusCode(400);
            }

            @Test
            void shouldReturn400WhenTotalAmountIsMissing() {

                String id = createRecurringExpense();

                RecurringExpenseUpdateDto body = new RecurringExpenseUpdateDto(
                        "Internet",
                        null,
                        PaymentType.PIX,
                        null,
                        null,
                        null
                );

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(body)
                        .when()
                        .put("/{id}", id)
                        .then()
                        .statusCode(400);
            }

            @Test
            void shouldReturn401WhenUserIsNotAuthenticated() {

                String id = UUID.randomUUID().toString();

                given()
                        .contentType(ContentType.JSON)
                        .body(validUpdateRequest())
                        .when()
                        .put("/{id}", id)
                        .then()
                        .statusCode(401);
            }
        }
    }

    @Nested
    class DeactivateRecurringExpense {

        @Test
        void shouldDeactivateRecurringExpenseSuccessfully() {

            String id = createRecurringExpense();

            given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .delete("/{id}", id)
                    .then()
                    .statusCode(204);
        }

        @Test
        void shouldReturn204WhenRecurringExpenseIsAlreadyInactive() {

            String id = createRecurringExpense();

            given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .delete("/{id}", id)
                    .then()
                    .statusCode(204);

            given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .delete("/{id}", id)
                    .then()
                    .statusCode(204);
        }

        @Test
        void shouldReturn404WhenRecurringExpenseDoesNotExist() {

            String id = UUID.randomUUID().toString();

            given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .delete("/{id}", id)
                    .then()
                    .statusCode(404);
        }

        @Test
        void shouldReturn404WhenRecurringExpenseBelongsToAnotherUser() {

            String id = createRecurringExpense();

            String anotherToken = createAnotherUserAndGetToken();

            given()
                    .header("Authorization", "Bearer " + anotherToken)
                    .when()
                    .delete("/{id}", id)
                    .then()
                    .statusCode(404);
        }

        @Test
        void shouldReturn401WhenUserIsNotAuthenticated() {

            String id = UUID.randomUUID().toString();

            given()
                    .when()
                    .delete("/{id}", id)
                    .then()
                    .statusCode(401);
        }
    }
}

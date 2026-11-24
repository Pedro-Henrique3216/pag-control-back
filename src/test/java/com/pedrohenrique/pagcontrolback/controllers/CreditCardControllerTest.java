package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.helpers.AuthTestFactory;
import com.pedrohenrique.pagcontrolback.helpers.DatabaseCleaner;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CreditCardControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AuthTestFactory authTestFactory;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    private String token;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/credit_cards";

        databaseCleaner.clearDatabase();

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

    // =========================================================
    // CREATE CREDIT CARD
    // =========================================================

    @Nested
    class CreateCreditCard {

        @Nested
        class Success {

            @Test
            void shouldCreateCreditCardSuccessfully() {
                String requestBody = """
                        {
                            "name": "Nubank",
                            "closing_day": 10,
                            "due_day": 17,
                            "credit_limit": 5000.00
                        }
                        """;

                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body(requestBody)
                        .when()
                        .post()
                        .then()
                        .statusCode(201)
                        .header("Location", containsString("/credit_cards/"))
                        .body("id", notNullValue())
                        .body("name", equalTo("Nubank"))
                        .body("closing_day", equalTo(10))
                        .body("due_day", equalTo(17))
                        .body("credit_limit", equalTo(5000.00F))
                        .body("active", equalTo(true));
            }
        }

        @Nested
        class Errors {

            @Test
            void shouldReturnBadRequestWhenClosingDayIsInvalid() {
                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body("""
                                {
                                    "name": "Nubank",
                                    "closing_day": 32,
                                    "due_day": 17,
                                    "credit_limit": 5000.00
                                }
                                """)
                        .when()
                        .post()
                        .then()
                        .statusCode(400);
            }

            @Test
            void shouldReturnBadRequestWhenDueDayIsInvalid() {
                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body("""
                                {
                                    "name": "Nubank",
                                    "closing_day": 10,
                                    "due_day": 0,
                                    "credit_limit": 5000.00
                                }
                                """)
                        .when()
                        .post()
                        .then()
                        .statusCode(400);
            }

            @Test
            void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() {
                given()
                        .contentType(ContentType.JSON)
                        .body("""
                                {
                                    "name": "Nubank",
                                    "closing_day": 10,
                                    "due_day": 17,
                                    "credit_limit": 5000.00
                                }
                                """)
                        .when()
                        .post()
                        .then()
                        .statusCode(401);
            }
        }
    }

    // =========================================================
    // LIST CREDIT CARDS
    // =========================================================

    @Nested
    class ListCreditCards {

        @Nested
        class Success {

            @Test
            void shouldReturnCreditCardsOfAuthenticatedUser() {
                createCreditCard("Nubank", 10, 17, 5000.00);
                createCreditCard("Inter", 5, 12, 3000.00);

                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .body("size()", equalTo(2))
                        .body("name", containsInAnyOrder("Nubank", "Inter"));
            }

            @Test
            void shouldReturnEmptyListWhenUserHasNoCreditCards() {
                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .body("size()", equalTo(0));
            }
        }

        @Nested
        class Errors {

            @Test
            void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() {
                given()
                        .when()
                        .get()
                        .then()
                        .statusCode(401);
            }
        }
    }

    // =========================================================
    // GET CREDIT CARD BY ID
    // =========================================================

    @Nested
    class GetCreditCardById {

        @Nested
        class Success {

            @Test
            void shouldReturnCreditCardById() {
                String creditCardId = createCreditCard(
                        "Nubank",
                        10,
                        17,
                        3000.00
                );

                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get("/" + creditCardId)
                        .then()
                        .statusCode(200)
                        .body("id", equalTo(creditCardId))
                        .body("name", equalTo("Nubank"))
                        .body("closing_day", equalTo(10))
                        .body("due_day", equalTo(17))
                        .body("credit_limit", equalTo(3000.00F))
                        .body("active", equalTo(true));
            }
        }

        @Nested
        class Errors {

            @Test
            void shouldReturnNotFoundWhenCreditCardDoesNotExist() {
                String creditCardId = java.util.UUID.randomUUID().toString();

                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get("/" + creditCardId)
                        .then()
                        .statusCode(404);
            }

            @Test
            void shouldReturnNotFoundWhenCreditCardBelongsToAnotherUser() {
                String creditCardId = createCreditCard(
                        "Nubank",
                        10,
                        17,
                        3000.00
                );

                String anotherUserToken = createAnotherUserAndGetToken();

                given()
                        .header("Authorization", "Bearer " + anotherUserToken)
                        .when()
                        .get("/" + creditCardId)
                        .then()
                        .statusCode(404);
            }

            @Test
            void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() {
                given()
                        .when()
                        .get("/" + java.util.UUID.randomUUID())
                        .then()
                        .statusCode(401);
            }
        }
    }

    // =========================================================
    // LIST CREDIT CARD EXPENSES
    // =========================================================

    @Nested
    class ListCreditCardExpenses {

        @Nested
        class Success {

            @Test
            void shouldReturnEmptyListWhenCreditCardHasNoExpenses() {
                String creditCardId = createCreditCard(
                        "Nubank",
                        10,
                        17,
                        3000.00
                );

                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get("/" + creditCardId + "/expenses")
                        .then()
                        .statusCode(200)
                        .body("size()", equalTo(0));
            }

            @Test
            void shouldReturnEmptyListWhenCreditCardDoesNotExist() {
                String creditCardId = java.util.UUID.randomUUID().toString();

                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get("/" + creditCardId + "/expenses")
                        .then()
                        .statusCode(200)
                        .body("size()", equalTo(0));
            }

            @Test
            void shouldReturnEmptyListWhenCreditCardBelongsToAnotherUser() {
                String creditCardId = createCreditCard(
                        "Nubank",
                        10,
                        17,
                        3000.00
                );

                String anotherUserToken = createAnotherUserAndGetToken();

                given()
                        .header("Authorization", "Bearer " + anotherUserToken)
                        .when()
                        .get("/" + creditCardId + "/expenses")
                        .then()
                        .statusCode(200)
                        .body("size()", equalTo(0));
            }
        }

        @Nested
        class Errors {

            @Test
            void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() {
                given()
                        .when()
                        .get("/" + java.util.UUID.randomUUID() + "/expenses")
                        .then()
                        .statusCode(401);
            }
        }
    }

    // =========================================================
    // UPDATE CREDIT CARD
    // =========================================================

    @Nested
    class UpdateCreditCard {

        @Nested
        class Success {

            @Test
            void shouldUpdateCreditCardSuccessfully() {
                String creditCardId = createCreditCard(
                        "Nubank",
                        10,
                        17,
                        2000.00
                );

                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body("""
                                {
                                    "name": "Inter",
                                    "closing_day": 15,
                                    "due_day": 25,
                                    "credit_limit": 5000.00
                                }
                                """)
                        .when()
                        .put("/" + creditCardId)
                        .then()
                        .statusCode(200)
                        .body("id", equalTo(creditCardId))
                        .body("name", equalTo("Inter"))
                        .body("closing_day", equalTo(15))
                        .body("due_day", equalTo(25))
                        .body("credit_limit", equalTo(5000.00F))
                        .body("active", equalTo(true));
            }

            @Test
            void shouldUpdateCreditCardPartially() {
                String creditCardId = createCreditCard(
                        "Nubank",
                        10,
                        17,
                        2000.00
                );

                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body("""
                                {
                                    "name": "Inter"
                                }
                                """)
                        .when()
                        .put("/" + creditCardId)
                        .then()
                        .statusCode(200)
                        .body("name", equalTo("Inter"))
                        .body("closing_day", equalTo(10))
                        .body("due_day", equalTo(17))
                        .body("credit_limit", equalTo(2000.00F));
            }
        }

        @Nested
        class Errors {

            @Test
            void shouldReturnNotFoundWhenCreditCardDoesNotExist() {
                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body("""
                                {
                                    "name": "Inter",
                                    "closing_day": 15,
                                    "due_day": 25,
                                    "credit_limit": 5000.00
                                }
                                """)
                        .when()
                        .put("/" + java.util.UUID.randomUUID())
                        .then()
                        .statusCode(404);
            }

            @Test
            void shouldReturnNotFoundWhenCreditCardBelongsToAnotherUser() {
                String creditCardId = createCreditCard(
                        "Nubank",
                        10,
                        17,
                        2000.00
                );

                String anotherUserToken = createAnotherUserAndGetToken();

                given()
                        .header("Authorization", "Bearer " + anotherUserToken)
                        .contentType(ContentType.JSON)
                        .body("""
                                {
                                    "name": "Inter",
                                    "closing_day": 15,
                                    "due_day": 25,
                                    "credit_limit": 5000.00
                                }
                                """)
                        .when()
                        .put("/" + creditCardId)
                        .then()
                        .statusCode(404);
            }

            @Test
            void shouldReturnBadRequestWhenClosingDayIsInvalid() {
                String creditCardId = createCreditCard(
                        "Nubank",
                        10,
                        17,
                        2000.00
                );

                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body("""
                                {
                                    "closing_day": 32
                                }
                                """)
                        .when()
                        .put("/" + creditCardId)
                        .then()
                        .statusCode(400);
            }

            @Test
            void shouldReturnBadRequestWhenDueDayIsInvalid() {
                String creditCardId = createCreditCard(
                        "Nubank",
                        10,
                        17,
                        2000.00
                );

                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body("""
                                {
                                    "due_day": 0
                                }
                                """)
                        .when()
                        .put("/" + creditCardId)
                        .then()
                        .statusCode(400);
            }

            @Test
            void shouldReturnBadRequestWhenNameIsBlank() {
                String creditCardId = createCreditCard(
                        "Nubank",
                        10,
                        17,
                        2000.00
                );

                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body("""
                                {
                                    "name": ""
                                }
                                """)
                        .when()
                        .put("/" + creditCardId)
                        .then()
                        .statusCode(400);
            }

            @Test
            void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() {
                given()
                        .contentType(ContentType.JSON)
                        .body("""
                                {
                                    "name": "Inter",
                                    "closing_day": 15,
                                    "due_day": 25,
                                    "credit_limit": 5000.00
                                }
                                """)
                        .when()
                        .put("/" + java.util.UUID.randomUUID())
                        .then()
                        .statusCode(401);
            }
        }
    }

    // =========================================================
    // DEACTIVATE CREDIT CARD
    // =========================================================

    @Nested
    class DeactivateCreditCard {

        @Nested
        class Success {

            @Test
            void shouldDeactivateCreditCardSuccessfully() {
                String creditCardId = createCreditCard(
                        "Nubank",
                        10,
                        17,
                        3000.00
                );

                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .patch("/" + creditCardId + "/deactivate")
                        .then()
                        .statusCode(204);

                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .body(
                                "find { it.id == '" + creditCardId + "' }.active",
                                is(false)
                        );
            }

            @Test
            void shouldReturnNoContentWhenCreditCardIsAlreadyInactive() {
                String creditCardId = createCreditCard(
                        "Nubank",
                        10,
                        17,
                        3000.00
                );

                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .patch("/" + creditCardId + "/deactivate")
                        .then()
                        .statusCode(204);

                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .patch("/" + creditCardId + "/deactivate")
                        .then()
                        .statusCode(204);
            }
        }

        @Nested
        class Errors {

            @Test
            void shouldReturnNotFoundWhenCreditCardDoesNotExist() {
                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .patch("/" + java.util.UUID.randomUUID() + "/deactivate")
                        .then()
                        .statusCode(404);
            }

            @Test
            void shouldReturnNotFoundWhenCreditCardBelongsToAnotherUser() {
                String creditCardId = createCreditCard(
                        "Nubank",
                        10,
                        17,
                        3000.00
                );

                String anotherUserToken = createAnotherUserAndGetToken();

                given()
                        .header("Authorization", "Bearer " + anotherUserToken)
                        .when()
                        .patch("/" + creditCardId + "/deactivate")
                        .then()
                        .statusCode(404);
            }

            @Test
            void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() {
                given()
                        .when()
                        .patch("/" + java.util.UUID.randomUUID() + "/deactivate")
                        .then()
                        .statusCode(401);
            }
        }
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private String createCreditCard(
            String name,
            int closingDay,
            int dueDay,
            double creditLimit
    ) {
        return given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "%s",
                            "closing_day": %d,
                            "due_day": %d,
                            "credit_limit": %.2f
                        }
                        """.formatted(name, closingDay, dueDay, creditLimit))
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
}

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
                String requestBody = """
                        {
                            "name": "Nubank",
                            "closing_day": 32,
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
                        .statusCode(400);
            }

            @Test
            void shouldReturnBadRequestWhenDueDayIsInvalid() {
                String requestBody = """
                        {
                            "name": "Nubank",
                            "closing_day": 10,
                            "due_day": 0,
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
                        .statusCode(400);
            }

            @Test
            void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() {
                String requestBody = """
                        {
                            "name": "Nubank",
                            "closing_day": 10,
                            "due_day": 17,
                            "credit_limit": 5000.00
                        }
                        """;

                given()
                        .contentType(ContentType.JSON)
                        .body(requestBody)
                        .when()
                        .post()
                        .then()
                        .statusCode(401);
            }
        }
    }

    @Nested
    class ListCreditCards {

        @Nested
        class Success {

            @Test
            void shouldReturnCreditCardsOfAuthenticatedUser() {
                String firstCard = """
                    {
                        "name": "Nubank",
                        "closing_day": 10,
                        "due_day": 17,
                        "credit_limit": 5000.00
                    }
                    """;

                String secondCard = """
                    {
                        "name": "Inter",
                        "closing_day": 5,
                        "due_day": 12,
                        "credit_limit": 3000.00
                    }
                    """;

                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body(firstCard)
                        .when()
                        .post()
                        .then()
                        .statusCode(201);

                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body(secondCard)
                        .when()
                        .post()
                        .then()
                        .statusCode(201);

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

    @Nested
    class UpdateCreditCard {

        @Nested
        class Success {

            @Test
            void shouldUpdateCreditCardSuccessfully() {
                String requestBody = """
                        {
                            "name": "Inter",
                            "closing_day": 15,
                            "due_day": 25,
                            "credit_limit": 5000.00
                        }
                        """;

                String creditCardId =
                        given()
                                .header("Authorization", "Bearer " + token)
                                .contentType(ContentType.JSON)
                                .body("""
                                        {
                                            "name": "Nubank",
                                            "closing_day": 10,
                                            "due_day": 17,
                                            "credit_limit": 2000.00
                                        }
                                        """)
                                .when()
                                .post()
                                .then()
                                .statusCode(201)
                                .extract()
                                .path("id");

                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body(requestBody)
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
                String creditCardId =
                        given()
                                .header("Authorization", "Bearer " + token)
                                .contentType(ContentType.JSON)
                                .body("""
                                        {
                                            "name": "Nubank",
                                            "closing_day": 10,
                                            "due_day": 17,
                                            "credit_limit": 2000.00
                                        }
                                        """)
                                .when()
                                .post()
                                .then()
                                .statusCode(201)
                                .extract()
                                .path("id");

                String requestBody = """
                        {
                            "name": "Inter"
                        }
                        """;

                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body(requestBody)
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
                String creditCardId = java.util.UUID.randomUUID().toString();

                String requestBody = """
                        {
                            "name": "Inter",
                            "closing_day": 15,
                            "due_day": 25,
                            "credit_limit": 5000.00
                        }
                        """;

                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body(requestBody)
                        .when()
                        .put("/" + creditCardId)
                        .then()
                        .statusCode(404);
            }

            @Test
            void shouldReturnNotFoundWhenCreditCardBelongsToAnotherUser() {
                String creditCardId =
                        given()
                                .header("Authorization", "Bearer " + token)
                                .contentType(ContentType.JSON)
                                .body("""
                                        {
                                            "name": "Nubank",
                                            "closing_day": 10,
                                            "due_day": 17,
                                            "credit_limit": 2000.00
                                        }
                                        """)
                                .when()
                                .post()
                                .then()
                                .statusCode(201)
                                .extract()
                                .path("id");

                authTestFactory.createUser(
                        "Jane Doe",
                        "jane@gmail.com",
                        "Password123@",
                        "11987654321"
                );

                String anotherUserToken = authTestFactory.loginAndGetToken(
                        port,
                        "jane@gmail.com",
                        "Password123@"
                );

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
                String creditCardId =
                        given()
                                .header("Authorization", "Bearer " + token)
                                .contentType(ContentType.JSON)
                                .body("""
                                        {
                                            "name": "Nubank",
                                            "closing_day": 10,
                                            "due_day": 17,
                                            "credit_limit": 2000.00
                                        }
                                        """)
                                .when()
                                .post()
                                .then()
                                .statusCode(201)
                                .extract()
                                .path("id");

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
                String creditCardId =
                        given()
                                .header("Authorization", "Bearer " + token)
                                .contentType(ContentType.JSON)
                                .body("""
                                        {
                                            "name": "Nubank",
                                            "closing_day": 10,
                                            "due_day": 17,
                                            "credit_limit": 2000.00
                                        }
                                        """)
                                .when()
                                .post()
                                .then()
                                .statusCode(201)
                                .extract()
                                .path("id");

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
                String creditCardId =
                        given()
                                .header("Authorization", "Bearer " + token)
                                .contentType(ContentType.JSON)
                                .body("""
                                        {
                                            "name": "Nubank",
                                            "closing_day": 10,
                                            "due_day": 17,
                                            "credit_limit": 2000.00
                                        }
                                        """)
                                .when()
                                .post()
                                .then()
                                .statusCode(201)
                                .extract()
                                .path("id");

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
                String creditCardId = java.util.UUID.randomUUID().toString();

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
                        .put("/" + creditCardId)
                        .then()
                        .statusCode(401);
            }
        }
    }

    @Nested
    class DeactivateCreditCard {

        @Nested
        class Success {

            @Test
            void shouldDeactivateCreditCardSuccessfully() {
                String response = given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body("""
                            {
                                "name": "Nubank",
                                "closing_day": 10,
                                "due_day": 17,
                                "credit_limit": 3000.00
                            }
                            """)
                        .when()
                        .post()
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id");

                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .patch("/" + response + "/deactivate")
                        .then()
                        .statusCode(204);

                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .body("find { it.id == '" + response + "' }.active", is(false));
            }

            @Test
            void shouldReturnNoContentWhenCreditCardIsAlreadyInactive() {
                String creditCardId = given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body("""
                            {
                                "name": "Nubank",
                                "closing_day": 10,
                                "due_day": 17,
                                "credit_limit": 3000.00
                            }
                            """)
                        .when()
                        .post()
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id");

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
                String creditCardId = given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body("""
                            {
                                "name": "Nubank",
                                "closing_day": 10,
                                "due_day": 17,
                                "credit_limit": 3000.00
                            }
                            """)
                        .when()
                        .post()
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id");

                authTestFactory.createUser(
                        "Jane Doe",
                        "jane@gmail.com",
                        "Password123@",
                        "11912345678"
                );

                String anotherUserToken = authTestFactory.loginAndGetToken(
                        port,
                        "jane@gmail.com",
                        "Password123@"
                );

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
}


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
}

package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.LoginRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.request.UserRequestDto;
import com.pedrohenrique.pagcontrolback.model.PersonType;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/users";
    }

    @Nested
    class SignIn {

        @Nested
        class Success {

            @Test
            void shouldCreateUserSuccessfully() {

                UserRequestDto request = new UserRequestDto(
                        "John Doe",
                        "JD Supplies",
                        "testeCreate@gmail.com",
                        "12345678Ab@",
                        "(11)92222-3333",
                        PersonType.PJ
                );

                var response = RestAssured.given()
                        .contentType("application/json")
                        .body(request)
                        .when()
                        .post("/signin")
                        .then()
                        .statusCode(201)
                        .extract()
                        .response();

                assertEquals("John Doe", response.path("name"));
                assertEquals("JD Supplies", response.path("fantasy_name"));
            }
        }

        @Nested
        class Errors {

            @Test
            void shouldReturn400WhenInvalidFields() {

                UserRequestDto request = new UserRequestDto(
                        "Jane Doe",
                        "JD Supplies",
                        "invalid-email",
                        "12345678",
                        "92222-3333",
                        PersonType.PJ
                );

                var response = RestAssured.given()
                        .contentType("application/json")
                        .body(request)
                        .when()
                        .post("/signin")
                        .then()
                        .statusCode(400)
                        .extract()
                        .response();

                List<String> errors = response.path("errors");

                assertNotNull(errors);
                assertTrue(errors.contains("Email not valid"));
                assertTrue(errors.contains(
                        "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
                ));
                assertTrue(errors.contains("Phone not valid"));
            }
        }
    }

    @Nested
    class Login {

        @Nested
        class Success {

            @Test
            void shouldLoginSuccessfully() {

                UserRequestDto user = new UserRequestDto(
                        "John Doe",
                        "JD Supplies",
                        "testeLogin@gmail.com",
                        "12345678Ab@",
                        "(11)92222-3333",
                        PersonType.PJ
                );

                // cria usuário
                RestAssured.given()
                        .contentType("application/json")
                        .body(user)
                        .when()
                        .post("/signin")
                        .then()
                        .statusCode(201);

                LoginRequestDto login = new LoginRequestDto(
                        "testeLogin@gmail.com",
                        "12345678Ab@"
                );

                String token = RestAssured.given()
                        .contentType("application/json")
                        .body(login)
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("token");

                assertNotNull(token);
            }
        }

        @Nested
        class Errors {

            @Test
            void shouldReturn400WhenEmailIsInvalid() {

                LoginRequestDto login = new LoginRequestDto(
                        "invalid-email",
                        "12345678Ab@"
                );

                RestAssured.given()
                        .contentType("application/json")
                        .body(login)
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(400);
            }

            @Test
            void shouldReturn401WhenCredentialsAreInvalid() {

                UserRequestDto user = new UserRequestDto(
                        "John Doe",
                        "JD Supplies",
                        "testeError@gmail.com",
                        "12345678Ab@",
                        "(11)92222-3333",
                        PersonType.PJ
                );

                // cria usuário
                RestAssured.given()
                        .contentType("application/json")
                        .body(user)
                        .when()
                        .post("/signin")
                        .then()
                        .statusCode(201);

                LoginRequestDto login = new LoginRequestDto(
                        "testeError@gmail.com",
                        "senhaerrada"
                );

                RestAssured.given()
                        .contentType("application/json")
                        .body(login)
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(401);
            }
        }
    }
}
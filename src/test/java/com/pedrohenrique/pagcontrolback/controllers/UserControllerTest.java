package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.config.security.TokenService;
import com.pedrohenrique.pagcontrolback.dtos.request.LoginRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.request.UserRequestDto;
import com.pedrohenrique.pagcontrolback.model.PersonType;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerTest {

    @LocalServerPort
    private int port;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private TokenService tokenService;

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
                        "testeCreateSuccess@gmail.com",
                        "12345678Ab@",
                        "(11)92222-3333",
                        PersonType.PJ
                );

                var response = RestAssured.given()
                        .contentType("application/json")
                        .body(request)
                        .when()
                        .post("/sign-up")
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
                        .post("/sign-up")
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

            @Test
            void shouldReturn400WhenUserAlreadyExists() {

                UserRequestDto request = new UserRequestDto(
                        "John Doe",
                        "JD Supplies",
                        "testeCreate@gmail.com",
                        "12345678Ab@",
                        "(11)92222-3333",
                        PersonType.PJ
                );

                RestAssured.given()
                        .contentType("application/json")
                        .body(request)
                        .when()
                        .post("/sign-up")
                        .then()
                        .statusCode(201)
                        .extract()
                        .response();


                RestAssured.given()
                        .contentType("application/json")
                        .body(request)
                        .when()
                        .post("/sign-up")
                        .then()
                        .statusCode(400)
                        .extract()
                        .response();
            }
        }
    }

    @Nested
    class Login {

        @Nested
        class Success {

            @Test
            void shouldLoginSuccessfully() {

                User user = new User(
                        "John Doe",
                        "JD Supplies",
                        "testeLogin@gmail.com",
                        bCryptPasswordEncoder.encode("12345678Ab@"),
                        "(11)92222-3333",
                        PersonType.PJ
                );

                user.verifyEmail();

                userRepository.save(user);

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

                User user = new User(
                        "John Doe",
                        "JD Supplies",
                        "testeError@gmail.com",
                        bCryptPasswordEncoder.encode("12345678Ab@"),
                        "(11)92222-3333",
                        PersonType.PJ
                );

                user.verifyEmail();
                userRepository.save(user);

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

            @Test
            void shouldReturn403WhenEmailIsNotVerified() {
                User user = new User(
                        "John Doe",
                        "JD Supplies",
                        "testeNotVerified@gmail.com",
                        bCryptPasswordEncoder.encode("12345678Ab@"),
                        "(11)92222-3333",
                        PersonType.PJ
                );

                userRepository.save(user);

                LoginRequestDto login = new LoginRequestDto(
                        "testeNotVerified@gmail.com",
                        "12345678Ab@"
                );

                RestAssured.given()
                        .contentType("application/json")
                        .body(login)
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(403);

            }
        }
    }

    @Nested
    class ResendConfirmation {

        @Nested
        class Success {

            @Test
            void shouldResendConfirmationEmailSuccessfully() {

                User user = new User(
                        "Pedro",
                        null,
                        "pedro@gmail.com",
                        bCryptPasswordEncoder.encode("12345678Ab@"),
                        "(11)99999-9999",
                        PersonType.PF
                );

                userRepository.save(user);

                var body = Map.of(
                        "email", "pedro@gmail.com"
                );

                RestAssured.given()
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post("/resend-confirmation")
                        .then()
                        .statusCode(200);
            }

            @Test
            void shouldReturn200WhenUserDoesNotExist() {

                var body = Map.of(
                        "email", "naoexiste@gmail.com"
                );

                RestAssured.given()
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post("/resend-confirmation")
                        .then()
                        .statusCode(200);
            }
        }

        @Nested
        class Errors {

            @Test
            void shouldReturn400WhenEmailIsInvalid() {

                var body = Map.of(
                        "email", "email-invalido"
                );

                RestAssured.given()
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post("/resend-confirmation")
                        .then()
                        .statusCode(400);
            }



            @Test
            void shouldReturn429WhenResendLimitIsExceeded(){

                User user = new User(
                        "Pedro",
                        null,
                        "pedroErro429@gmail.com",
                        bCryptPasswordEncoder.encode("12345678Ab@"),
                        "(11)99999-9999",
                        PersonType.PF
                );

                userRepository.save(user);

                var body = Map.of(
                        "email", "pedroErro429@gmail.com"
                );

                resendConfirmationEmail(user.getEmail().value());
                resendConfirmationEmail(user.getEmail().value());
                resendConfirmationEmail(user.getEmail().value());

                RestAssured.given()
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post("/resend-confirmation")
                        .then()
                        .statusCode(429);
            }
        }

        private void resendConfirmationEmail(String email) {
            RestAssured.given()
                    .contentType("application/json")
                    .body(Map.of("email", email))
                    .when()
                    .post("/resend-confirmation");
        }
    }

    @Nested
    class ConfirmEmail {

        @Nested
        class Success {

            @Test
            void shouldRedirectToSuccessPageWhenEmailIsConfirmed() {

                User user = new User(
                        "Pedro",
                        null,
                        "testeConfirmEmail@gmail.com",
                        bCryptPasswordEncoder.encode("12345678Ab@"),
                        "(11)99999-9999",
                        PersonType.PF
                );

                userRepository.save(user);

                String token = tokenService.generateTokenToEmailConfirmation(
                        user.getId()
                );

                RestAssured.given()
                        .redirects().follow(false)
                        .queryParam("token", token)
                        .when()
                        .get("/confirm")
                        .then()
                        .statusCode(302)
                        .header("Location", "http://localhost:3000/email-confirmed");
            }
        }


        @Nested
        class Errors {

            @Test
            void shouldRedirectToInvalidPageWhenTokenIsInvalid() {

                RestAssured.given()
                        .redirects().follow(false)
                        .queryParam("token", "invalid-token")
                        .when()
                        .get("/confirm")
                        .then()
                        .statusCode(302)
                        .header("Location", "http://localhost:3000/email-invalid");
            }
        }
    }
}
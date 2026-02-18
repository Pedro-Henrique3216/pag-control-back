package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.UserRequestDto;
import com.pedrohenrique.pagcontrolback.model.PersonType;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
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

    @Test
    void shouldCreateUserSuccessfully() {
        UserRequestDto request = new UserRequestDto(
                "John Doe",
                "JD Supplies",
                "teste@gmail.com",
                "12345678Ab@",
                "(11)92222-3333",
                PersonType.PJ
        );

        var response = RestAssured.given()
                .contentType("application/json")
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .response();

        assertNotNull(response);
        assertEquals("John Doe", response.path("name"));
        assertEquals("JD Supplies", response.path("fantasy_name"));
    }

    @Test
    void whenInvalidFields_thenReturnsBadRequest() {
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
                .post()
                .then()
                .statusCode(400)
                .extract()
                .response();

        assertNotNull(response);
        List<String> errors = response.path("errors");

        assertNotNull(errors);

        assertTrue(errors.contains("Email not valid"));

        assertTrue(errors.contains(
                "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
        ));

        assertTrue(errors.contains("Phone not valid"));
    }

}
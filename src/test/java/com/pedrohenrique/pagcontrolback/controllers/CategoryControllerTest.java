package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.CategoryRequestDto;
import com.pedrohenrique.pagcontrolback.helpers.AuthTestFactory;
import com.pedrohenrique.pagcontrolback.model.CategoryType;
import com.pedrohenrique.pagcontrolback.model.PersonType;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CategoryControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthTestFactory authTestFactory;

    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/categories";

        userRepository.deleteAll();

        user = userRepository.save(
                new User(
                        "John Doe",
                        null,
                        "testeCategory@gmail.com",
                        passwordEncoder.encode("password123"),
                        "12345678900",
                        PersonType.PF
                )
        );

        token = authTestFactory.loginAndGetToken(
                port,
                "testeCategory@gmail.com",
                "password123"
        );
    }

    @Test
    void shouldCreateCategorySuccessfully() {



        CategoryRequestDto body = new CategoryRequestDto(
                "food",
                CategoryType.EXPENSE
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("food"))
                .body("category_type", equalTo("EXPENSE"));
    }

    @Test
    void shouldReturn400WhenNameIsEmpty() {

        CategoryRequestDto body = new CategoryRequestDto(
                "",
                CategoryType.EXPENSE
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
    void shouldReturn400WhenCategoryTypeIsMissing() {

        CategoryRequestDto body = new CategoryRequestDto(
                "food",
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
    void shouldReturn409WhenCategoryAlreadyExists() {

        CategoryRequestDto body = new CategoryRequestDto(
                "food",
                CategoryType.EXPENSE
        );


        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(409);
    }

    @Test
    void shouldReturn401WhenUserNotAuthenticated() {

        CategoryRequestDto body = new CategoryRequestDto(
                "food",
                CategoryType.EXPENSE
        );

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/categories")
                .then()
                .statusCode(403);
    }

}
package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.CategoryRequestDto;
import com.pedrohenrique.pagcontrolback.helpers.AuthTestFactory;
import com.pedrohenrique.pagcontrolback.helpers.CategoryFactory;
import com.pedrohenrique.pagcontrolback.model.CategoryType;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
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
    private AuthTestFactory authTestFactory;

    @Autowired
    private CategoryFactory categoryFactory;

    private String token;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/categories";

        userRepository.deleteAll();

        authTestFactory.createUser(
                "John Doe",
                "teste@gmail.com",
                "Password123@",
                "11912345678",
                port
        );

        token = authTestFactory.loginAndGetToken(
                port,
                "teste@gmail.com",
                "Password123@"
        );
    }


    @Nested
    class CreateCategory {

        @Nested
        class Success {

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
        }

        @Nested
        class Errors {

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
                        .post()
                        .then()
                        .statusCode(401);
            }
        }
    }


    @Nested
    class GetCategories {

        @Nested
        class Success {

            @Test
            void shouldReturn200WhenFindAllCategoriesByUser() {

                categoryFactory.createCategoryExpense(port, token);

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .body("size()", equalTo(1))
                        .body("[0].id", notNullValue())
                        .body("[0].name", equalTo("teste"))
                        .body("[0].category_type", equalTo("EXPENSE"));
            }
        }
    }
}
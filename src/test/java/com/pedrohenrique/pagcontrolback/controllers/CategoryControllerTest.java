package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.CategoryRequestDto;
import com.pedrohenrique.pagcontrolback.helpers.AuthTestFactory;
import com.pedrohenrique.pagcontrolback.helpers.CategoryFactory;
import com.pedrohenrique.pagcontrolback.helpers.DatabaseCleaner;
import com.pedrohenrique.pagcontrolback.model.TransactionType;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CategoryControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AuthTestFactory authTestFactory;

    @Autowired
    private CategoryFactory categoryFactory;

    @Autowired
    private DatabaseCleaner cleaner;

    private String token;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/categories";

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

    private String createCategoryAndGetId() {
        CategoryRequestDto body = new CategoryRequestDto(
                "food",
                TransactionType.EXPENSE
        );

        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    @Nested
    class CreateCategory {

        @Nested
        class Success {

            @Test
            void shouldCreateCategorySuccessfully() {

                CategoryRequestDto body = new CategoryRequestDto(
                        "food",
                        TransactionType.EXPENSE
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
                        TransactionType.EXPENSE
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
                        TransactionType.EXPENSE
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
                        TransactionType.EXPENSE
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
                        .body("[0].name", equalToIgnoringCase("teste"))
                        .body("[0].category_type", equalTo("EXPENSE"));
            }
        }
    }

    @Nested
    class GetCategoryById {

        @Nested
        class Success {

            @Test
            void shouldReturn200WhenCategoryExists() {

                String categoryId = createCategoryAndGetId();

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get("/{categoryId}", categoryId)
                        .then()
                        .statusCode(200)
                        .body("id", equalTo(categoryId))
                        .body("name", equalTo("food"))
                        .body("category_type", equalTo("EXPENSE"));
            }
        }

        @Nested
        class Errors {

            @Test
            void shouldReturn404WhenCategoryDoesNotExist() {

                String categoryId = UUID.randomUUID().toString();

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get("/{categoryId}", categoryId)
                        .then()
                        .statusCode(404);
            }

            @Test
            void shouldReturn401WhenUserNotAuthenticated() {

                String categoryId = UUID.randomUUID().toString();

                given()
                        .contentType(ContentType.JSON)
                        .when()
                        .get("/{categoryId}", categoryId)
                        .then()
                        .statusCode(401);
            }
        }
    }

    @Nested
    class UpdateCategory {

        @Nested
        class Success {

            @Test
            void shouldUpdateCategorySuccessfully() {

                String categoryId = createCategoryAndGetId();

                CategoryRequestDto body = new CategoryRequestDto(
                        "transport",
                        TransactionType.EXPENSE
                );

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(body)
                        .when()
                        .put("/{categoryId}", categoryId)
                        .then()
                        .statusCode(200)
                        .body("id", equalTo(categoryId))
                        .body("name", equalTo("transport"))
                        .body("category_type", equalTo("EXPENSE"));
            }

            @Test
            void shouldUpdateCategoryTypeSuccessfully() {

                String categoryId = createCategoryAndGetId();

                CategoryRequestDto body = new CategoryRequestDto(
                        "salary",
                        TransactionType.INCOME
                );

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(body)
                        .when()
                        .put("/{categoryId}", categoryId)
                        .then()
                        .statusCode(200)
                        .body("name", equalTo("salary"))
                        .body("category_type", equalTo("INCOME"));
            }
        }

        @Nested
        class Errors {

            @Test
            void shouldReturn404WhenCategoryDoesNotExist() {

                String categoryId = UUID.randomUUID().toString();

                CategoryRequestDto body = new CategoryRequestDto(
                        "transport",
                        TransactionType.EXPENSE
                );

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(body)
                        .when()
                        .put("/{categoryId}", categoryId)
                        .then()
                        .statusCode(404);
            }

            @Test
            void shouldReturn400WhenNameIsEmpty() {

                String categoryId = createCategoryAndGetId();

                CategoryRequestDto body = new CategoryRequestDto(
                        "",
                        TransactionType.EXPENSE
                );

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(body)
                        .when()
                        .put("/{categoryId}", categoryId)
                        .then()
                        .statusCode(400);
            }

            @Test
            void shouldReturn400WhenCategoryTypeIsMissing() {

                String categoryId = createCategoryAndGetId();

                CategoryRequestDto body = new CategoryRequestDto(
                        "transport",
                        null
                );

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(body)
                        .when()
                        .put("/{categoryId}", categoryId)
                        .then()
                        .statusCode(400);
            }

            @Test
            void shouldReturn401WhenUserNotAuthenticated() {

                String categoryId = UUID.randomUUID().toString();

                CategoryRequestDto body = new CategoryRequestDto(
                        "transport",
                        TransactionType.EXPENSE
                );

                given()
                        .contentType(ContentType.JSON)
                        .body(body)
                        .when()
                        .put("/{categoryId}", categoryId)
                        .then()
                        .statusCode(401);
            }
        }
    }

    @Nested
    class DeactivateCategory {

        @Nested
        class Success {

            @Test
            void shouldReturn204WhenCategoryIsDeactivated() {

                String categoryId = createCategoryAndGetId();

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .delete("/{categoryId}", categoryId)
                        .then()
                        .statusCode(204);
            }
        }

        @Nested
        class Errors {

            @Test
            void shouldReturn404WhenCategoryDoesNotExist() {

                String categoryId = UUID.randomUUID().toString();

                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .delete("/{categoryId}", categoryId)
                        .then()
                        .statusCode(404);
            }

            @Test
            void shouldReturn401WhenUserNotAuthenticated() {

                String categoryId = UUID.randomUUID().toString();

                given()
                        .contentType(ContentType.JSON)
                        .when()
                        .delete("/{categoryId}", categoryId)
                        .then()
                        .statusCode(401);
            }
        }
    }
}

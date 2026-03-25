package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.SupplierRequestDto;
import com.pedrohenrique.pagcontrolback.helpers.AuthTestFactory;
import com.pedrohenrique.pagcontrolback.helpers.SupplierFactory;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SupplierControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SupplierFactory factory;

    @Autowired
    private AuthTestFactory authFactory;

    private String token;

    @BeforeEach
    void setUp() {

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/suppliers";

        userRepository.deleteAll();

        authFactory.createUser(
                "teste",
                "teste@gmail.com",
                "Password123@",
                "11999999999",
                port
        );

        token = authFactory.loginAndGetToken(
                port,
                "teste@gmail.com",
                "Password123@"
        );
    }

    @Test
    void whenCreateSupplier_thenReturn201() {

        SupplierRequestDto requestDto = new SupplierRequestDto(
                "Supplier Name",
                "12.345.678/0001-95"
        );

        var response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .response();

        assertNotNull(response);
        assertEquals("Supplier Name", response.jsonPath().getString("name"));
    }

    @Test
    void whenRequestBodyIsInvalid_thenReturn400() {

        SupplierRequestDto requestDto = new SupplierRequestDto(
                "",
                "invalid-cnpj"
        );

        var response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post()
                .then()
                .statusCode(400)
                .extract()
                .response();

        assertNotNull(response);

        List<String> errors = response.path("errors");

        assertNotNull(errors);
        assertTrue(errors.contains("name must not be blank"));
    }

    @Test
    void whenCnpjIsInvalid_thenReturn400() {

        SupplierRequestDto requestDto = new SupplierRequestDto(
                "Supplier Name",
                "invalid-cnpj"
        );

        var response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post()
                .then()
                .statusCode(400)
                .extract()
                .response();

        assertNotNull(response);

        List<String> errors = response.path("errors");

        assertNotNull(errors);
        assertTrue(errors.contains("Invalid CNPJ format."));
    }

    @Test
    void whenSupplierWithSameCnpjAlreadyExists_thenReturn409() {

        factory.createSupplier(
                "Existing Supplier",
                "12.345.678/0001-95",
                port,
                token
        );

        SupplierRequestDto requestDto = new SupplierRequestDto(
                "Supplier Name",
                "12.345.678/0001-95"
        );

        var response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post()
                .then()
                .statusCode(409)
                .extract()
                .response();

        assertNotNull(response);

        List<String> errors = response.path("errors");

        assertNotNull(errors);
        assertTrue(errors.contains("Supplier with this CNPJ already exists for this user."));
    }

    @Test
    void shouldReturnSuppliersWhenUserHasSuppliers() {

        factory.createSupplier("Fornecedor 1", null, port, token);
        factory.createSupplier("Fornecedor 2", null, port, token);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("$.size()", is(2));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoSuppliers() {

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("$.size()", is(0));
    }

    @Test
    void shouldReturnSupplierWhenSupplierIdIsValidAndBelongsToUser() {

        factory.createSupplier("Fornecedor 1", null, port, token);

        Supplier supplier = supplierRepository.findAll().get(0);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .get("/{supplierId}", supplier.getId())
                .then()
                .statusCode(200)
                .body("name", is("Fornecedor 1"));
    }

    @Test
    void shouldReturn404WhenSupplierIsNotFound() {

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .get("/{supplierId}", UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn404WhenSupplierDoesNotBelongToUser() {

        authFactory.createUser(
                "Other User",
                "other@gmail.com",
                "Password123@",
                "11912345678",
                port
        );

        String otherToken = authFactory.loginAndGetToken(
                port,
                "other@gmail.com",
                "Password123@"
        );

        UUID supplierId = factory.createSupplier("Fornecedor de outro usuário", null, port, otherToken);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .get("/{supplierId}", supplierId)
                .then()
                .statusCode(404);
    }
}

package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.SupplierRequestDto;
import com.pedrohenrique.pagcontrolback.helpers.TestDataFactory;
import com.pedrohenrique.pagcontrolback.model.PersonType;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SupplierControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private TestDataFactory factory;

    private User user;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/suppliers";

        userRepository.deleteAll();
        supplierRepository.deleteAll();


        user = userRepository.save(
                new User(
                        "John Doe",
                        null,
                        "testeSupplier@gmail.com",
                        "password123",
                        "12345678900",
                        PersonType.PF
                )
        );
    }

    @Test
    void whenCreateSupplier_thenReturn201(){
        SupplierRequestDto requestDto = new SupplierRequestDto(
                "Supplier Name",
                "12.345.678/0001-95"
        );

        var response = RestAssured.given()
                .contentType("application/json")
                .body(requestDto)
                .when()
                .post("/{userId}", user.getId())
                .then()
                .statusCode(201)
                .extract()
                .response();
        ;
        assertNotNull(response);
        assertEquals("Supplier Name", response.jsonPath().getString("name"));
    }

    @Test
    void whenRequestBodyIsInvalid_thenReturn400(){
        SupplierRequestDto requestDto = new SupplierRequestDto(
                "",
                "invalid-cnpj"
        );

        var response = RestAssured.given()
                .contentType("application/json")
                .body(requestDto)
                .when()
                .post("/{userId}", user.getId())
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
    void whenUserIdIsInvalid_thenReturn400(){
        SupplierRequestDto requestDto = new SupplierRequestDto(
                "Supplier Name",
                "12.345.678/0001-95"
        );

        var response = RestAssured.given()
                .contentType("application/json")
                .body(requestDto)
                .when()
                .post("/{userId}", "invalid-uuid")
                .then()
                .statusCode(400)
                .extract()
                .response();

        assertNotNull(response);
    }

    @Test
    void whenUserNotFound_thenReturn404(){
        SupplierRequestDto requestDto = new SupplierRequestDto(
                "Supplier Name",
                "12.345.678/0001-95"
        );

        var userId = UUID.randomUUID();

        var response = RestAssured.given()
                .contentType("application/json")
                .body(requestDto)
                .when()
                .post("/{userId}", userId)
                .then()
                .statusCode(404)
                .extract()
                .response();

        assertNotNull(response);
        List<String> errors = response.path("errors");
        assertNotNull(errors);
        assertTrue(errors.contains("User not found with ID: "+ userId));
    }

    @Test
    void whenCnpjIsInvalid_thenReturn400(){
        SupplierRequestDto requestDto = new SupplierRequestDto(
                "Supplier Name",
                "invalid-cnpj"
        );

        var response = RestAssured.given()
                .contentType("application/json")
                .body(requestDto)
                .when()
                .post("/{userId}", user.getId())
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
    void whenSupplierWithSameCnpjAlreadyExists_thenReturn409(){
        Supplier supplier = new Supplier(
                "Existing Supplier",
                "12.345.6780001-95"
        );
        supplier.setUser(user);
        supplierRepository.save(supplier);
        SupplierRequestDto requestDto = new SupplierRequestDto(
                "Supplier Name",
                "12.345.678/0001-95"
        );

        var response = RestAssured.given()
                .contentType("application/json")
                .body(requestDto)
                .when()
                .post("/{userId}", user.getId())
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

        factory.createSupplier(user.getId(), "Fornecedor 1", null, port);
        factory.createSupplier(user.getId(), "Fornecedor 2", null, port);

        RestAssured
                .given()
                .accept(ContentType.JSON)
                .when()
                .get("/{userId}", user.getId())
                .then()
                .statusCode(200)
                .body("$.size()", is(2));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoSuppliers(){
        RestAssured
                .given()
                .accept(ContentType.JSON)
                .when()
                .get("/{userId}", user.getId())
                .then()
                .statusCode(200)
                .body("$.size()", is(0));
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist(){
        var userId = UUID.randomUUID();

        RestAssured
                .given()
                .accept(ContentType.JSON)
                .when()
                .get("/{userId}", userId)
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn400WhenUserIdIsInvalid(){
        RestAssured
                .given()
                .accept(ContentType.JSON)
                .when()
                .get("/{userId}", "invalid-uuid")
                .then()
                .statusCode(400);
    }
    @Test
    void shouldReturnSupplierWhenUserIdAndSupplierIdAreValid(){
        factory.createSupplier(user.getId(), "Fornecedor 1", null, port);

        Supplier supplier = supplierRepository.findAll().get(0);

        RestAssured
                .given()
                .accept(ContentType.JSON)
                .when()
                .get("/{userId}/{supplierId}", user.getId(), supplier.getId())
                .then()
                .statusCode(200)
                .body("name", is("Fornecedor 1"));
    }

    @Test
    void shouldReturn404WhenSupplierIsNotFoundForUser(){
        var supplierId = UUID.randomUUID();

        RestAssured
                .given()
                .accept(ContentType.JSON)
                .when()
                .get("/{userId}/{supplierId}", user.getId(), supplierId)
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn404WhenGetByUserIdAndSupplierIdUserDoesNotExist(){
        var userId = UUID.randomUUID();
        var supplierId = UUID.randomUUID();

        RestAssured
                .given()
                .accept(ContentType.JSON)
                .when()
                .get("/{userId}/{supplierId}", userId, supplierId)
                .then()
                .statusCode(404);
    }

}
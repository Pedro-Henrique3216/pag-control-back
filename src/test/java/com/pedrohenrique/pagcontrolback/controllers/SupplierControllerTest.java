package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.SupplierRequestDto;
import com.pedrohenrique.pagcontrolback.model.PersonType;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SupplierControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

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

}
package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.ExpenseRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.ExpenseResponseDto;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExpenseControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/expenses";


        supplierRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(
                new User(
                        "John Doe",
                        null,
                        "testeExpense@gmail.com",
                        "password123",
                        "12345678900",
                        PersonType.PF
                )
        );

        supplier = supplierRepository.save(
                new Supplier("Supplier Inc.")
        );

    }

    @Test
    void whenCreateExpenseWithInstallments_thenReturn201(){
        ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto(
                null,
                PaymentType.CREDIT,
                supplier.getId(),
                LocalDate.of(2026, 2, 2),
                new HashMap<>(){
                    {
                        put(30, "1234567890123456");
                        put(60, "9876543210987654");
                    }
                },
                BigDecimal.valueOf(400.00)
        );
        var response = RestAssured
                .given()
                .contentType("application/json")
                .body(expenseRequestDto)
                .when()
                .post("/{userId}", user.getId())
                .then()
                .statusCode(201)
                .extract()
                .response();

        ExpenseResponseDto expenseResponseDto = response.body().as(ExpenseResponseDto.class);

        assertNotNull(response);
        assertEquals(200, expenseResponseDto.installments().get(0).amount().intValue());
        assertEquals(2, expenseResponseDto.installments().size());
        assertEquals(LocalDate.of(2026, 2, 2), expenseResponseDto.date());
        assertEquals("9876543210987654", expenseResponseDto.installments().get(1).barcode());
        assertEquals(InstallmentStatus.UNPAID, expenseResponseDto.installments().get(0).status());
    }

    @Test
    void whenRequestBodyIsInvalid_thenReturn400(){
        ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto(
                null,
                null,
                supplier.getId(),
                LocalDate.of(2026, 2, 2),
                null,
                null
        );
        var response = RestAssured
                .given()
                .contentType("application/json")
                .body(expenseRequestDto)
                .when()
                .post("/{userId}", user.getId())
                .then()
                .statusCode(400)
                .extract()
                .response();

        assertNotNull(response);

        List<String> errors = response.path("errors");

        assertNotNull(errors);

        assertTrue(errors.contains("Payment type is required"));

        assertTrue(errors.contains("Total amount is required"));
    }

    @Test
    void whenUserNotFound_thenReturn404(){
        ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto(
                null,
                PaymentType.CREDIT,
                supplier.getId(),
                LocalDate.of(2026, 2, 2),
                new HashMap<>(){
                    {
                        put(30, "1234567890123456");
                        put(60, "9876543210987654");
                    }
                },
                BigDecimal.valueOf(400.00)
        );

        var randomUserId = UUID.randomUUID();
        var response = RestAssured
                .given()
                .contentType("application/json")
                .body(expenseRequestDto)
                .when()
                .post("/{userId}", randomUserId)
                .then()
                .statusCode(404)
                .extract()
                .response();

        assertNotNull(response);
        List<String> errors = response.path("errors");
        assertNotNull(errors);
        assertTrue(errors.contains("User not found with id: " + randomUserId));
    }

    @Test
    void whenSupplierNotFound_thenReturn404(){
        var randomSupplierId = UUID.randomUUID();

        ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto(
                null,
                PaymentType.CREDIT,
                randomSupplierId,
                LocalDate.of(2026, 2, 2),
                new HashMap<>(){
                    {
                        put(30, "1234567890123456");
                        put(60, "9876543210987654");
                    }
                },
                BigDecimal.valueOf(400.00)
        );

        var response = RestAssured
                .given()
                .contentType("application/json")
                .body(expenseRequestDto)
                .when()
                .post("/{userId}", user.getId())
                .then()
                .statusCode(404)
                .extract()
                .response();

        assertNotNull(response);
        List<String> errors = response.path("errors");
        assertNotNull(errors);
        assertTrue(errors.contains("Supplier not found with id: " + randomSupplierId));
    }

    @Test
    void whenInstallmentsAreMissing_thenReturn400(){
        ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto(
                null,
                PaymentType.CREDIT,
                supplier.getId(),
                LocalDate.of(2026, 2, 2),
                null,
                BigDecimal.valueOf(400.00)
        );
        var response = RestAssured
                .given()
                .contentType("application/json")
                .body(expenseRequestDto)
                .when()
                .post("/{userId}", user.getId())
                .then()
                .statusCode(400)
                .extract()
                .response();

        assertNotNull(response);
        List<String> errors = response.path("errors");
        assertNotNull(errors);
        assertTrue(errors.contains("Installment intervals must be provided for CREDIT or BILL payment types."));
    }

    @Test
    void whenInstallmentDueInDaysIsInvalid_thenReturn400(){
        ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto(
                null,
                PaymentType.CREDIT,
                supplier.getId(),
                LocalDate.of(2026, 2, 2),
                new HashMap<>(){
                    {
                        put(-1, "1234567890123456");
                    }
                },
                BigDecimal.valueOf(400.00)
        );
        var response = RestAssured
                .given()
                .contentType("application/json")
                .body(expenseRequestDto)
                .when()
                .post("/{userId}", user.getId())
                .then()
                .statusCode(400)
                .extract()
                .response();

        assertNotNull(response);
        List<String> errors = response.path("errors");
        assertNotNull(errors);
        assertTrue(errors.contains("Installment due in days must be greater than zero."));
    }

}
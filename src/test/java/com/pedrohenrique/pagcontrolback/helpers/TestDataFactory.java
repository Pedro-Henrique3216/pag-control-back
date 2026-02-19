package com.pedrohenrique.pagcontrolback.helpers;

import com.pedrohenrique.pagcontrolback.dtos.request.ExpenseRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.request.SupplierRequestDto;
import com.pedrohenrique.pagcontrolback.model.PaymentType;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

@Component
public class TestDataFactory {

    public void createExpense(
            UUID userId,
            UUID supplierId,
            String invoiceNumber,
            LocalDate date,
            int port,
            String token
    ) {

        ExpenseRequestDto dto = new ExpenseRequestDto(
                invoiceNumber,
                PaymentType.CASH,
                supplierId,
                date,
                null,
                BigDecimal.valueOf(100)
        );

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .when()
                .post("http://localhost:" + port + "/api/expenses/{userId}", userId)
                .then()
                .statusCode(201);
    }


    public void createExpense(
            UUID userId,
            UUID supplierId,
            String invoiceNumber,
            int port,
            String token
    ) {
        createExpense(
                userId,
                supplierId,
                invoiceNumber,
                LocalDate.of(2026, 2, 2),
                port,
                token
        );
    }

    public void createExpense(
            UUID userId,
            UUID supplierId,
            String invoiceNumber,
            BigDecimal amount,
            int port,
            String token
    ) {

        ExpenseRequestDto dto = new ExpenseRequestDto(
                invoiceNumber,
                PaymentType.CREDIT,
                supplierId,
                LocalDate.of(2026, 2, 2),
                new HashMap<>(){{
                    put(27, null);
                    put(40, null);
                }},
                amount
        );

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .when()
                .post("http://localhost:" + port + "/api/expenses/{userId}", userId)
                .then()
                .statusCode(201);
    }

    public void createExpense(
            UUID userId,
            UUID supplierId,
            String invoiceNumber,
            BigDecimal amount,
            LocalDate date,
            int port,
            String token
    ) {

        ExpenseRequestDto dto = new ExpenseRequestDto(
                invoiceNumber,
                PaymentType.CREDIT,
                supplierId,
                date,
                new HashMap<>() {{
                    put(5, null);
                    put(10, null);
                }},
                amount
        );

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .when()
                .post("http://localhost:" + port + "/api/expenses/{userId}", userId)
                .then()
                .statusCode(201);
    }

    public void createSupplier(
            UUID userId,
            String name,
            String cnpj,
            int port,
            String token
    ) {

        SupplierRequestDto dto = new SupplierRequestDto(
                name,
                cnpj
        );
        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .when()
                .post("http://localhost:" + port + "/api/suppliers/{userId}", userId)
                .then()
                .statusCode(201);
    }


}

package com.pedrohenrique.pagcontrolback.helpers;

import com.pedrohenrique.pagcontrolback.dtos.request.ExpenseRequestDto;
import com.pedrohenrique.pagcontrolback.model.PaymentType;
import com.pedrohenrique.pagcontrolback.model.RecurrenceType;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

@Component
public class ExpenseFactory {

    public void createExpense(
            UUID supplierId,
            String invoiceNumber,
            LocalDate date,
            int port,
            String token
    ) {

        ExpenseRequestDto dto = new ExpenseRequestDto(
                invoiceNumber,
                "Teste",
                PaymentType.CASH,
                supplierId,
                date,
                null,
                BigDecimal.valueOf(100),
                null,
                false,
                null,
                null,
                null
        );

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .when()
                .post("http://localhost:" + port + "/api/expenses")
                .then()
                .statusCode(201);
    }

    public void createExpense(
            UUID supplierId,
            String invoiceNumber,
            BigDecimal amount,
            LocalDate date,
            int port,
            String token
    ) {

        ExpenseRequestDto dto = new ExpenseRequestDto(
                invoiceNumber,
                "Teste",
                PaymentType.CREDIT,
                supplierId,
                date,
                new HashMap<>() {{
                    put(5, null);
                    put(10, null);
                }},
                amount,
                null,
                false,
                null,
                null,
                null
        );

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .when()
                .post("http://localhost:" + port + "/api/expenses")
                .then()
                .statusCode(201);
    }

    public void createExpense(
            UUID supplierId,
            String invoiceNumber,
            BigDecimal amount,
            LocalDate date,
            UUID categoryId,
            int port,
            String token
    ) {

        ExpenseRequestDto dto = new ExpenseRequestDto(
                invoiceNumber,
                "Teste",
                PaymentType.CREDIT,
                supplierId,
                date,
                new HashMap<>() {{
                    put(5, null);
                    put(10, null);
                }},
                amount,
                categoryId,
                false,
                null,
                null,
                null
        );

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .when()
                .post("http://localhost:" + port + "/api/expenses")
                .then()
                .statusCode(201);
    }

    public void createExpenseRecurrence(
            UUID supplierId,
            String invoiceNumber,
            BigDecimal amount,
            LocalDate date,
            int port,
            String token)
    {
        ExpenseRequestDto dto = new ExpenseRequestDto(
                invoiceNumber,
                "Teste",
                PaymentType.CREDIT,
                supplierId,
                date,
                null,
                amount,
                null,
                true,
                RecurrenceType.MONTHLY,
                1,
                null
        );

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .when()
                .post("http://localhost:" + port + "/api/expenses")
                .then()
                .statusCode(201);
    }
}

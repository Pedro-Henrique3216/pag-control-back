package com.pedrohenrique.pagcontrolback.helpers;

import com.pedrohenrique.pagcontrolback.dtos.request.ExpenseRequestDto;
import com.pedrohenrique.pagcontrolback.model.PaymentType;
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
                PaymentType.CASH,
                supplierId,
                date,
                null,
                BigDecimal.valueOf(100),
                null
        );

        String response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .when()
                .post("http://localhost:" + port + "/api/expenses")
                .then()
                .statusCode(201)
                .extract()
                .response()
                .asString();

        System.out.println(response);
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
                PaymentType.CREDIT,
                supplierId,
                date,
                new HashMap<>() {{
                    put(5, null);
                    put(10, null);
                }},
                amount,
                null
        );

        String response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .when()
                .post("http://localhost:" + port + "/api/expenses")
                .then()
                .statusCode(201)
                .extract()
                .response()
                .asString();

        System.out.println(response);
    }


}

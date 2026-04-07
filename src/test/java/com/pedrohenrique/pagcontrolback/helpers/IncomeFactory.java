package com.pedrohenrique.pagcontrolback.helpers;

import com.pedrohenrique.pagcontrolback.dtos.request.IncomeRequestDto;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class IncomeFactory {

    public void createIncome(
            BigDecimal amount,
            String description,
            LocalDate date,
            int port,
            String token
    ) {

        IncomeRequestDto body = new IncomeRequestDto(
                amount,
                description,
                date,
                null
        );

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when()
                .post("http://localhost:" + port + "/api/incomes")
                .then()
                .statusCode(201);
    }
}

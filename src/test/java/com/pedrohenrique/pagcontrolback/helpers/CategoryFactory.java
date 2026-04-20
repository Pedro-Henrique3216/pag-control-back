package com.pedrohenrique.pagcontrolback.helpers;

import com.pedrohenrique.pagcontrolback.dtos.request.CategoryRequestDto;
import com.pedrohenrique.pagcontrolback.model.TransactionType;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CategoryFactory {

    public UUID createCategoryExpense(
            int port,
            String token
    ) {
        CategoryRequestDto dto = new CategoryRequestDto(
                "Teste",
                TransactionType.EXPENSE
        );
        String id =  RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .when()
                .post("http://localhost:" + port + "/api/categories")
                .then()
                .statusCode(201)
                .extract().body().path("id");

        return UUID.fromString(id);
    }

    public UUID createCategoryIncome(
            int port,
            String token
    ) {
        CategoryRequestDto dto = new CategoryRequestDto(
                "Teste",
                TransactionType.INCOME
        );
        String id =  RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .when()
                .post("http://localhost:" + port + "/api/categories")
                .then()
                .statusCode(201)
                .extract().body().path("id");

        return UUID.fromString(id);
    }
}

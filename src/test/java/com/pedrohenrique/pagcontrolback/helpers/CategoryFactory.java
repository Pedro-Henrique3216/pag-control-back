package com.pedrohenrique.pagcontrolback.helpers;

import com.pedrohenrique.pagcontrolback.dtos.request.CategoryRequestDto;
import com.pedrohenrique.pagcontrolback.model.CategoryType;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CategoryFactory {

    public UUID createCategory(
            int port,
            String token
    ) {
        CategoryRequestDto dto = new CategoryRequestDto(
                "Teste",
                CategoryType.EXPENSE
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

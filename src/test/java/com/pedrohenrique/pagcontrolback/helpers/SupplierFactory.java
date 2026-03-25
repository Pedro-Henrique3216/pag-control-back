package com.pedrohenrique.pagcontrolback.helpers;

import com.pedrohenrique.pagcontrolback.dtos.request.SupplierRequestDto;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SupplierFactory {

    public UUID createSupplier(
            String name,
            String cnpj,
            int port,
            String token
    ) {

        SupplierRequestDto dto = new SupplierRequestDto(
                name,
                cnpj
        );
        return UUID.fromString(RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .when()
                .post("http://localhost:" + port + "/api/suppliers")
                .then()
                .statusCode(201)
                .extract().body().path("id"));
    }

}

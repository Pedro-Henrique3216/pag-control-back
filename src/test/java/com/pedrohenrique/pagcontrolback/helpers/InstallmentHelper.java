package com.pedrohenrique.pagcontrolback.helpers;

import io.restassured.RestAssured;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class InstallmentHelper {

    public List<UUID> getInstallments(String token, int port) {
        return RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("http://localhost:" + port + "/api/installments")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("id")
                .stream()
                .map(id -> UUID.fromString((String) id))
                .toList();
    }

    public void payInstallment(UUID id, String token, int port) {
        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("http://localhost:" + port + "/api/installments/" + id + "/pay")
                .then()
                .statusCode(200);
    }
}

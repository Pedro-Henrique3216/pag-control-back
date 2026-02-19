package com.pedrohenrique.pagcontrolback.helpers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthTestFactory {

    public String loginAndGetToken(int port, String email, String password) {

        var body = Map.of(
                "email", email,
                "password", password
        );

        return RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("http://localhost:" + port + "/api/users/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }
}

package com.pedrohenrique.pagcontrolback.helpers;

import com.pedrohenrique.pagcontrolback.dtos.request.UserRequestDto;
import com.pedrohenrique.pagcontrolback.model.PersonType;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthTestFactory {

    public void createUser(
            String name,
            String email,
            String password,
            String phone,
            int port){

        var body = new UserRequestDto(
                name,
                null,
                email,
                password,
                phone,
                PersonType.PF
        );

        var response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("http://localhost:" + port + "/api/users/signin")
                .then()
                .statusCode(201)
                .extract()
                .response()
                .asString();

        System.out.println(response);
    }

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

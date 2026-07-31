package com.pedrohenrique.pagcontrolback.helpers;

import com.pedrohenrique.pagcontrolback.model.PersonType;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthTestFactory {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public void createUser(
            String name,
            String email,
            String password,
            String phone
    ){

        User user = new User(
                name,
                null,
                email,
                passwordEncoder.encode(password),
                phone,
                PersonType.PF
        );

        user.verifyEmail();

        userRepository.save(user);
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

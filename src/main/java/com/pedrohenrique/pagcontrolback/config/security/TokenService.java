package com.pedrohenrique.pagcontrolback.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.pedrohenrique.pagcontrolback.exceptions.InvalidTokenException;
import com.pedrohenrique.pagcontrolback.exceptions.TokenGenerationException;
import com.pedrohenrique.pagcontrolback.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(String email, UUID userId) {
        try {
            return JWT.create()
                    .withSubject(email)
                    .withClaim("id", userId.toString())
                    .withIssuer("pagcontrol")
                    .withExpiresAt(generateExpirationDate())
                    .sign(Algorithm.HMAC256(secret));
        } catch (JWTCreationException exception) {
            throw new TokenGenerationException("Error generating token", exception);
        }
    }

    public UUID getUserId(String token) {
        try {
            return UUID.fromString(
                    JWT.require(Algorithm.HMAC256(secret))
                            .withIssuer("pagcontrol")
                            .build()
                            .verify(token)
                            .getClaim("id")
                            .asString()
            );
        } catch (JWTVerificationException exception) {
            throw new InvalidTokenException("Invalid token");
        }
    }

    private Instant generateExpirationDate(){
        return LocalDateTime.now().plusDays(5).toInstant(ZoneOffset.of("-03:00"));
    }
}

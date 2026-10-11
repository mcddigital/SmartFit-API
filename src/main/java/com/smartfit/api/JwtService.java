
package com.smartfit.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    private final Algorithm algorithm;

    public JwtService(@Value("${JWT_SECRET}") String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                "JWT_SECRET deve ter pelo menos 32 caracteres"
            );
        }

        this.algorithm = Algorithm.HMAC256(secret);
    }

    public String gerarToken(String email) {
        return JWT.create()
                .withIssuer("smartfit-api")
                .withSubject(email)
                .withIssuedAt(Instant.now())
                .withExpiresAt(
                    Instant.now().plus(24, ChronoUnit.HOURS)
                )
                .sign(algorithm);
    }

    public String validarToken(String token) {
        try {
            return JWT.require(algorithm)
                    .withIssuer("smartfit-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException(
                "Token inválido ou expirado", e
            );
        }
    }
}

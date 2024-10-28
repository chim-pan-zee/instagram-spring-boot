package com.example.instagram_spring_boot.util;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;

// import lombok.extern.slf4j.Slf4j;
// @Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretValue;

    private static String secret;

    @PostConstruct
    public void init() {
        secret = secretValue;
    }

    public String createToken(String idx, String username, String name) {
        Algorithm algorithm = Algorithm.HMAC256(secret);

        Calendar date = Calendar.getInstance();
        long timeInSecs = date.getTimeInMillis();
        Date now = new Date();

        String accessToken = JWT.create()
                .withClaim("useridx", idx)
                .withClaim("username", username)
                .withClaim("name", name)
                .withIssuedAt(now)
                .withExpiresAt(new Date(System.currentTimeMillis() + 1 * (1000 * 60 * 30)))
                .sign(algorithm);

        String refreshToken = JWT.create()
                .withClaim("useridx", idx)
                .withClaim("username", username)
                .withClaim("name", name)
                .withIssuedAt(now)
                .withExpiresAt(new Date(System.currentTimeMillis() + 1 * (1000 * 60 * 60 * 24 * 7)))
                .sign(algorithm);

        return accessToken;
    }

    public DecodedJWT decodeToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("vue-board")
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            System.out.println("error");
        } catch (IllegalArgumentException e) {
            System.out.println("error");
        }

        return null;
    }

    public String validateToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}

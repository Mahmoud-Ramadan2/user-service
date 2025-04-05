package com.mahmoud.appointmentsystem.user_service.security;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;


@Component
public class JWTUtil {

    @Value("${JWTSecretKey}")
    private String JWTSecretKey;
    @Value("${JWTExpiration}")
    private long expirationTime;

    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {

        return JWT.create()
                .withSubject("User_Details")
                .withClaim("username", username)
                .withClaim("roles", authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .withIssuer("appointments/system")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(expirationTime + System.currentTimeMillis()))
                .sign(Algorithm.HMAC256(JWTSecretKey));
    }

    public String validateTokenAndRetrieveUsername(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(JWTSecretKey))
                    .withSubject("User_Details")
                    .withIssuer("appointments/system")
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);

            return decodedJWT.getClaim("username").toString();


        } catch (JWTVerificationException e) {
            throw new RuntimeException("Invalid or expired JWT token", e);
        }

    }

}

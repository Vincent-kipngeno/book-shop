package com.example.apigateway;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "VgS8dEKGfUG30s5g9CwEgOo9LpaKQIagv+SVzrnkZZdhTJwlReKCAGKOnafamMEqbx9kIEe23XYLFam+74Nlozmn76oVD+FneVbyC4EqxMmccmTiaT+JpqOCNM7M/VBuJNHX3xCveL65sl+EXijFMpYz0X1oBseei/71rS6HoKsZ0OVIzA0T/8xCNLBqjr61mSkekdMVZDCzteWLAE1hyadN85Xlp8+ZBdFYQ2GYWfiYdqdThkERiwebxEyOWNQahurutcRUvR6SjCbr5urSdyRxKNAqYp2GXumSVkDA0D6bz0Ns7ADCKsCjjmXMrIGNlcnHPLgjymzeQThXvl6zZ+f6UCfzKhtZDFpE/aRM510=\r\n";

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(Base64.getEncoder().encodeToString(SECRET_KEY.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            JwtParser parser = Jwts.parser().verifyWith(getSignInKey()).build();
            parser.parseSignedClaims(token); // Throws if invalid
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        JwtParser parser = Jwts.parser().verifyWith(getSignInKey()).build();
        return parser.parseSignedClaims(token).getPayload().getSubject();
    }
}

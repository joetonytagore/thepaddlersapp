package org.thepaddlers.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long ttlMs = 24 * 60 * 60 * 1000L; // 24h

    public JwtService(@Value("${jwt.secret:dev-secret}") String secret) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).build();
    }

    public String issueToken(Map<String, String> claims, String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlMs);
        var builder = JWT.create().withSubject(subject).withIssuedAt(now).withExpiresAt(exp);
        for (var e : claims.entrySet()) {
            builder.withClaim(e.getKey(), e.getValue());
        }
        return builder.sign(algorithm);
    }

    public DecodedJWT verify(String token) {
        return verifier.verify(token);
    }
}


package com.system.libraryManagementSystem.security;

import com.system.libraryManagementSystem.exception.MemberNotFoundException;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    @Autowired
    MemberRepository memberRepository;

    private final String SECRET_KEY = "CE899FEAB8E9D262B88FA266AA68ABBEF8F748D1C502E466BD9E2424BB51282F3707BCE469FBC9B039A652494B0CA938CF5E01157941E4C2C774C77473C3890D";
    private final Long EXPIRATION = TimeUnit.HOURS.toMillis(24);


    public String getToken(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with the email: " + email));

        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "LMS");  //LMS name of app
        claims.put("roles", member.getRoles());

        return Jwts.builder()
                .claims(claims)
                .subject(member.getEmail())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(EXPIRATION)))
                .signWith(generateKey())
                .compact();

    }

    private SecretKey generateKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractEmail(String jwt) {
        Claims claims = getPayload(jwt);
        return claims.getSubject();
    }

    private Claims getPayload(String jwt) {
        return Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    public boolean isTokenValid(String jwt) {
        Claims claims = getPayload(jwt);
        return claims.getExpiration().after(Date.from(Instant.now()));
    }
}

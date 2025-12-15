package com.example.backend.auth.service.Impl;


import com.example.backend.entity.*;
import com.example.backend.auth.service.JWTservice;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;


@Service
@Primary
public class JWTserviceImpl implements JWTservice {


    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getKey() {
        return key;
    }

//    // 15 minutes for access token
//    private static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000;

    // 2 months (60 days)
    private static final long ACCESS_TOKEN_EXPIRATION =
            60L * 24 * 60 * 60 * 1000;


    // 7 days for refresh token
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    //generating access token
    @Override
    public String generateToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        // Add a single role as well, if you want
        claims.put("role", user.getRole().name());

        // Pass username and roles set to buildToken
        Set<Role> roles = Set.of(user.getRole()); // assuming user has a single Role
        return buildToken(claims, user.getEmail(), roles, ACCESS_TOKEN_EXPIRATION);
    }

    //generating refresh token
    @Override
    public String generateRefreshToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        // Optional: keep single role as a convenience
        claims.put("role", user.getRole().name());

        // Convert the single Role to a Set to pass to buildToken
        Set<Role> roles = Set.of(user.getRole());

        return buildToken(claims, user.getEmail(), roles, REFRESH_TOKEN_EXPIRATION);
    }

    //build token
    private String buildToken(Map<String, Object> claims, String username, Set<Role> roles, long expiration) {
        claims.put("roles", roles.stream().map(Enum::name).toList());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    //returning roles from token
    public List<String> extractRoles(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return (List<String>) claims.get("roles");
    }


//    //generate key for signing
//    private SecretKey getkey() {
//        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }


    //getting username from token
    public String extractUsername(String token) {
        //extracting username from token
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build().parseSignedClaims(token).getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUsername(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


}
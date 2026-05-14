package com.vtn.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    // Access token: ngắn hạn (vd: 15 phút = 900_000 ms)
    @Value("${jwt.expiration}")
    private long accessTokenExpiration;

    // Refresh token: dài hạn (vd: 7 ngày = 604_800_000 ms)
    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ─── Access Token ────────────────────────────────────────────────────────

    public String generateAccessToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(String username) {
        return generateAccessToken(new HashMap<>(), username);
    }

    // ─── Refresh Token ───────────────────────────────────────────────────────

    /**
     * Refresh token mang theo familyId + version để server kiểm tra.
     * Không cần lưu token string xuống DB — chỉ cần lưu family metadata.
     */
    public String generateRefreshToken(String username, String familyId, int version) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("familyId", familyId);
        claims.put("version", version);
        claims.put("type", "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ─── Extract / Validate ──────────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractFamilyId(String token) {
        return (String) extractAllClaims(token).get("familyId");
    }

    public Integer extractVersion(String token) {
        return (Integer) extractAllClaims(token).get("version");
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Parse token kể cả khi đã expired (dùng để đọc claims từ expired access token nếu cần).
     */
    public Claims extractAllClaimsIgnoreExpiry(String token) {
        try {
            return extractAllClaims(token);
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpiration;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
package com.joe.springsecurity.auth.service;

import com.joe.springsecurity.auth.model.User;
import com.joe.springsecurity.auth.repo.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    // Using HS512 to generate the secret key securely
    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    @Value("${application.security.jwt.access-token-expiration}")
    private long accessTokenExpire;

    @Value("${application.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpire;

    private final TokenRepository tokenRepository;

    public JwtService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    // Method to extract the username from the token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Method to validate the access token
    public boolean isValid(String token, UserDetails user) {
        String username = extractUsername(token);

        boolean validToken = tokenRepository
                .findByAccessToken(token)
                .map(t -> !t.isLoggedOut())
                .orElse(false);

        return (username.equals(user.getUsername())) && !isTokenExpired(token) && validToken;
    }

    // Method to validate the refresh token
    public boolean isValidRefreshToken(String token, User user) {
        logger.debug("Validating token for user: {}", user.getUsername());

        String username = extractUsername(token);

        boolean validRefreshToken = tokenRepository
                .findByRefreshToken(token)
                .map(t -> !t.isLoggedOut())
                .orElse(false);

        return (username.equals(user.getUsername())) && !isTokenExpired(token) && validRefreshToken;
    }

    // Check if the token has expired
//    private boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date());
//    }

    public boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        Date now = new Date();
        if (expiration.before(now)) {
            logger.warn("Token expired at {}", expiration);
            return true;
        }
        return false;
    }

    // Extract expiration from the token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Generic method to extract claims from the token
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    // Extract all claims from the token
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(SECRET_KEY)  // Use the generated HS512 key
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Generate the access token for the user
    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpire);
    }

    // Generate the refresh token for the user
    public String generateRefreshToken(User user) {
        logger.info("Generating access token for user: {}", user.getUsername());
        return generateToken(user, refreshTokenExpire);
    }

    // Helper method to generate a JWT token with expiration time
    private String generateToken(User user, long expireTime) {
        return Jwts.builder()
                .setSubject(user.getUsername())  // Set the username as the subject
                .setIssuedAt(new Date(System.currentTimeMillis()))  // Set the issue time
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))  // Set expiration time
                .signWith(SECRET_KEY)  // Sign the JWT with the HS512 key
                .compact();  // Generate the compact JWT
    }

    // Get the signing key (HS512) for the JWT
    private SecretKey getSigninKey() {
        return SECRET_KEY;  // Return the generated HS512 key
    }
}

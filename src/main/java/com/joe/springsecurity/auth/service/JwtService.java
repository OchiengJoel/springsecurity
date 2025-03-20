package com.joe.springsecurity.auth.service;

import com.joe.springsecurity.auth.model.User;
import com.joe.springsecurity.auth.repo.TokenRepository;
import com.joe.springsecurity.company.model.Company;
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

    // Method to extract the companyId from the token
    public Long extractCompanyId(String token) {
        return extractClaim(token, claims -> claims.get("companyId", Long.class));
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
    public Date extractExpiration(String token) {
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
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Generate the access token for the user
    public String generateAccessToken(User user, Company currentCompany) {
        return generateToken(user, currentCompany, accessTokenExpire);
    }

    // Generate the refresh token for the user
    public String generateRefreshToken(User user, Company currentCompany) {
        logger.info("Generating refresh token for user: {}", user.getUsername());
        return generateToken(user, currentCompany, refreshTokenExpire);
    }

    private String generateToken(User user, Company currentCompany, long expireTime) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .claim("companyId", currentCompany.getId())  // Store companyId instead of name
                .claim("companyName", currentCompany.getName())  // Optional: keep name for convenience
                .signWith(SECRET_KEY)
                .compact();
    }

    // Get the signing key (HS512) for the JWT
    private SecretKey getSigninKey() {
        return SECRET_KEY;
    }
}

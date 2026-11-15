package com.servicedesk.servicedesk_backend.security;
import com.servicedesk.servicedesk_backend.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.util.Date;
import java.util.Objects;

@Service
public class JwtService {

    // STEP 1: DECLARE JWT CONFIGURATION VARIABLES

    // Secret key is used to sign and verify JWT tokens.
    private final SecretKey signingKey;

    // Token expiration time in milliseconds.
    private final long jwtExpirationMs;


    // STEP 2: CONSTRUCTOR INJECTION OF JWT CONFIGURATION
    // Values will be fetched from application.properties
    // and the environment variable JWT_SECRET.
    public JwtService(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration-ms}") long jwtExpirationMs) {

        // Decode the Base64 secret into its original byte array.
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);

        // Convert the decoded bytes into a cryptographic SecretKey.
        // A minimum of 256 bits is required for HS256.
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);

        this.jwtExpirationMs = jwtExpirationMs;
    }


    // STEP 3: GENERATE JWT TOKEN
    // This method is called after successful user authentication.
    public String generateToken(User user) {

        // Get the current time.
        Date currentTime = new Date();

        // Calculate when the token should expire.
        Date expirationTime = new Date(
                currentTime.getTime() + jwtExpirationMs
        );

        // Build JWT with user information, timestamps and signature.
        return Jwts.builder()

                // Store the user's email as JWT subject.
                .subject(user.getEmail())

                // Store additional information as JWT claims.
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())

                // Set when the JWT was generated.
                .issuedAt(currentTime)

                // Set when the JWT expires.
                .expiration(expirationTime)

                // Digitally sign the JWT using HS256.
                .signWith(signingKey, Jwts.SIG.HS256)

                // Convert JWT into its final String representation.
                .compact();
    }


    // STEP 4: EXTRACT EMAIL FROM JWT TOKEN
    public String extractUsername(String token) {

        // Decode and verify token before accessing its subject.
        return extractAllClaims(token).getSubject();
    }


    // STEP 5: EXTRACT ALL CLAIMS FROM JWT
    private Claims extractAllClaims(String token) {

        return Jwts.parser()

                // Set the secret key used for signature verification.
                .verifyWith(signingKey)

                // Build the JWT parser.
                .build()

                // Parse the signed JWT and verify its signature.
                .parseSignedClaims(token)

                // Retrieve the payload containing JWT claims.
                .getPayload();
    }


    // STEP 6: CHECK WHETHER JWT TOKEN IS VALID
    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        try {

            // Extract all claims after verifying the token signature.
            Claims claims = extractAllClaims(token);

            // Extract email from JWT subject.
            String username = claims.getSubject();

            // Extract token expiration time.
            Date expirationTime = claims.getExpiration();

            // STEP 7: VALIDATE TOKEN CONDITIONS

            // Check whether JWT belongs to the authenticated user.
            boolean isUsernameMatching = Objects.equals(
                    username,
                    userDetails.getUsername()
            );

            // Check whether the JWT has not expired.
            boolean isTokenNotExpired =
                    expirationTime != null &&
                            expirationTime.after(new Date());

            // Check whether the user account is still enabled.
            boolean isUserEnabled = userDetails.isEnabled();

            // Token is valid only when all conditions are satisfied.
            return isUsernameMatching
                    && isTokenNotExpired
                    && isUserEnabled;

        } catch (JwtException | IllegalArgumentException exception) {

            // Invalid signature, malformed token or expired token.
            // Return false instead of allowing the exception to
            // break the authentication filter.
            return false;
        }
    }
}

package com.servicedesk.servicedesk_backend.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // STEP 1: DECLARE REQUIRED DEPENDENCIES

    // JwtService is responsible for extracting email
    // and validating the JWT token.
    private final JwtService jwtService;

    // CustomUserDetailsService retrieves user details from MySQL.
    private final CustomUserDetailsService userDetailsService;


    // STEP 2: CONSTRUCTOR INJECTION

    // Receive both dependencies when creating this filter.
    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }


    // STEP 3: OVERRIDE DOFILTERINTERNAL()

    // Spring automatically invokes this method when an HTTP
    // request passes through our JWT filter.
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {


        // STEP 4: EXTRACT AUTHORIZATION HEADER

        // The frontend/Postman sends the JWT inside this header:
        // Authorization: Bearer <JWT_TOKEN>
        final String authHeader = request.getHeader("Authorization");


        // STEP 5: CHECK WHETHER THE AUTHORIZATION HEADER EXISTS

        // If the header is missing or doesn't use the Bearer scheme,
        // we don't authenticate the request using JWT.
        // Continue the filter chain because some APIs are public.
        if (authHeader == null ||
                !authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {

            filterChain.doFilter(request, response);
            return;
        }


        // STEP 6: EXTRACT THE ACTUAL JWT TOKEN

        // Remove "Bearer " from the beginning of the header.
        // trim() removes accidental surrounding whitespace.
        final String jwtToken = authHeader.substring(7).trim();

        // Reject an empty Bearer token.
        if (jwtToken.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }


        try {

            // STEP 7: EXTRACT USER EMAIL FROM JWT

            // JwtService verifies the token signature while parsing it.
            // The email is stored in the JWT subject.
            String userEmail = jwtService.extractUsername(jwtToken);


            // STEP 8: CHECK WHETHER USER IS ALREADY AUTHENTICATED

            // If Spring Security already has an authenticated user
            // in the current SecurityContext, don't authenticate again.
            if (userEmail != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {


                // STEP 9: LOAD USER DETAILS FROM MYSQL

                // Fetch the user using CustomUserDetailsService.
                // This gives us the current password hash, role
                // and enabled status.
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(userEmail);


                // STEP 10: VALIDATE JWT AGAINST USER DETAILS

                // Check whether:
                // 1. JWT belongs to this user.
                // 2. JWT has not expired.
                // 3. User account is enabled.
                boolean isValid = jwtService.isTokenValid(
                        jwtToken,
                        userDetails
                );


                if (isValid) {

                    // STEP 11: CREATE AUTHENTICATION OBJECT

                    // UsernamePasswordAuthenticationToken has
                    // multiple constructors.
                    //
                    // This constructor represents an authenticated user
                    // because we provide the user's granted authorities.
                    //
                    // Credentials are null because we don't need to
                    // store the password in the SecurityContext.
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );


                    // STEP 12: ATTACH REQUEST DETAILS

                    // Stores request-related details such as
                    // remote address and session information.
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );


                    // STEP 13: SAVE AUTHENTICATION IN SECURITY CONTEXT

                    // Spring Security can now identify the current user
                    // and access their roles during authorization.
                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (JwtException |
                 IllegalArgumentException |
                 UsernameNotFoundException exception) {

            // STEP 14: HANDLE INVALID JWT OR UNKNOWN USER

            // If the JWT is malformed, expired, has an invalid
            // signature or belongs to a missing user,
            // remove any authentication from the current request.
            //
            // Protected endpoints will subsequently be rejected
            // by Spring Security's authorization rules.
            SecurityContextHolder.clearContext();
        }


        // STEP 15: CONTINUE THE FILTER CHAIN

        // Pass control to the next Spring Security filter.
        // Eventually, the request reaches the authorization checks
        // and then the appropriate controller if permitted.
        filterChain.doFilter(request, response);
    }
}

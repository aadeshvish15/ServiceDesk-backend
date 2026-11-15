
package com.servicedesk.servicedesk_backend.security;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.exception.ApiError;
import tools.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;


// STEP 2: DECLARE SECURITY CONFIGURATION CLASS

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // STEP 3: CONFIGURE PASSWORD ENCODER

    // BCrypt securely hashes passwords before storing them in MySQL.
    // Spring Security also uses it to verify login passwords.
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    // STEP 4: CONFIGURE AUTHENTICATION MANAGER

    // AuthenticationManager coordinates the username/password
    // authentication process during login.
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }


    // STEP 5: REGISTER JWT AUTHENTICATION FILTER

    // Create the custom JWT filter using constructor injection.
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService) {

        return new JwtAuthenticationFilter(
                jwtService,
                userDetailsService
        );
    }


    // STEP 6: CONFIGURE SECURITY FILTER CHAIN

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JsonMapper jsonMapper) throws Exception {


        // STEP 6.1: DISABLE CSRF

        // This is a stateless REST API using JWT authentication.
        http.csrf(AbstractHttpConfigurer::disable);


        // STEP 6.2: CONFIGURE SESSION MANAGEMENT

        // Spring Security will not create or use HTTP sessions.
        // Each protected request must provide a valid JWT.
        http.sessionManagement(session ->
                session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS
                )
        );


        // STEP 6.3: CONFIGURE ENDPOINT AUTHORIZATION

        http.authorizeHttpRequests(auth -> auth

                // PUBLIC REGISTRATION AND LOGIN
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/auth/register",
                        "/api/auth/login"
                ).permitAll()


                // PUBLIC HEALTH CHECK
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/health",
                        "/actuator/health",
                        "/actuator/health/**"
                ).permitAll()


                // CURRENT USER ENDPOINT
                // Requires a valid JWT.
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/auth/me"
                ).authenticated()


                // ADMIN-ONLY ENDPOINTS
                .requestMatchers("/api/admin/**")
                .hasRole("ADMIN")


                // ADMIN AND AGENT ENDPOINTS
                .requestMatchers("/api/agent/**")
                .hasAnyRole("ADMIN", "AGENT")


                // ADMIN AND EMPLOYEE ENDPOINTS
                .requestMatchers("/api/employee/**")
                .hasAnyRole("ADMIN", "EMPLOYEE")


                // ALL OTHER ENDPOINTS REQUIRE AUTHENTICATION
                .anyRequest().authenticated()
        );


        // STEP 6.4: CONFIGURE AUTHENTICATION AND AUTHORIZATION ERRORS

        http.exceptionHandling(exception -> exception

                // HANDLE UNAUTHORIZED REQUESTS (401)
                .authenticationEntryPoint(
                        (request, response, authException) -> {

                            writeSecurityError(
                                    response,
                                    jsonMapper,
                                    HttpStatus.UNAUTHORIZED,
                                    "Authentication is required or the JWT token is invalid",
                                    request.getRequestURI()
                            );
                        }
                )


                // HANDLE FORBIDDEN REQUESTS (403)
                .accessDeniedHandler(
                        (request, response, accessDeniedException) -> {

                            writeSecurityError(
                                    response,
                                    jsonMapper,
                                    HttpStatus.FORBIDDEN,
                                    "You do not have permission to access this resource",
                                    request.getRequestURI()
                            );
                        }
                )
        );


        // STEP 6.5: REGISTER JWT FILTER

        // Execute JWT authentication before the standard
        // UsernamePasswordAuthenticationFilter.
        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );


        // STEP 6.6: BUILD SECURITY FILTER CHAIN

        return http.build();
    }


    // STEP 7: CREATE REUSABLE SECURITY ERROR RESPONSE METHOD

    private void writeSecurityError(
            HttpServletResponse response,
            JsonMapper jsonMapper,
            HttpStatus status,
            String message,
            String path) throws IOException {


        // STEP 7.1: CREATE STANDARD API ERROR OBJECT

        ApiError apiError = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                Map.of()
        );


        // STEP 7.2: SET HTTP STATUS

        response.setStatus(status.value());


        // STEP 7.3: SET RESPONSE CHARACTER ENCODING

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );


        // STEP 7.4: SET RESPONSE CONTENT TYPE

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );


        // STEP 7.5: CONVERT API ERROR OBJECT TO JSON

        jsonMapper.writeValue(
                response.getOutputStream(),
                apiError
        );
    }
}

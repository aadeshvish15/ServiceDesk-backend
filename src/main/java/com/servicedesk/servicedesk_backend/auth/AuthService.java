
package com.servicedesk.servicedesk_backend.auth;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.auth.dto.AuthResponse;
import com.servicedesk.servicedesk_backend.auth.dto.LoginRequest;
import com.servicedesk.servicedesk_backend.auth.dto.RegisterRequest;
import com.servicedesk.servicedesk_backend.auth.dto.UserResponse;

import com.servicedesk.servicedesk_backend.exception.EmailAlreadyExistsException;

import com.servicedesk.servicedesk_backend.security.JwtService;

import com.servicedesk.servicedesk_backend.user.Role;
import com.servicedesk.servicedesk_backend.user.User;
import com.servicedesk.servicedesk_backend.user.UserRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;


// STEP 2: DECLARE AUTH SERVICE

// @Service tells Spring that this class contains business logic.
// The controller will call this class to perform authentication operations.
@Service
public class AuthService {

    // STEP 3: DECLARE REQUIRED DEPENDENCIES

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    // STEP 4: CONSTRUCTOR INJECTION

    // Spring automatically injects the required dependencies.
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }


    // STEP 5: IMPLEMENT EMPLOYEE REGISTRATION

    @Transactional
    public UserResponse register(RegisterRequest request) {

        // STEP 5.1: NORMALIZE EMAIL

        // Remove surrounding spaces and convert email to lowercase.
        // This prevents duplicate accounts caused by email casing.
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);


        // STEP 5.2: CHECK WHETHER EMAIL ALREADY EXISTS

        // Check the database before creating a new account.
        if (userRepository.existsByEmailIgnoreCase(email)) {

            throw new EmailAlreadyExistsException(
                    "An account with this email already exists"
            );
        }


        // STEP 5.3: HASH USER PASSWORD

        // Never store plain-text passwords in the database.
        // BCrypt converts the password into a secure hash.
        String encodedPassword = passwordEncoder.encode(
                request.password()
        );


        // STEP 5.4: CREATE NEW EMPLOYEE ACCOUNT

        // Public registration is restricted to EMPLOYEE.
        // Users cannot register themselves as ADMIN or AGENT.
        User user = new User(
                request.name().trim(),
                email,
                encodedPassword,
                Role.EMPLOYEE
        );


        // STEP 5.5: SAVE USER IN MYSQL

        // Spring Data JPA inserts the new user into the users table.
        User savedUser = userRepository.save(user);


        // STEP 5.6: RETURN SAFE USER INFORMATION

        // Do not expose the password or password hash.
        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }


    // STEP 6: IMPLEMENT USER LOGIN

    public AuthResponse login(LoginRequest request) {

        // STEP 6.1: NORMALIZE EMAIL

        // Use the same email normalization applied during registration.
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);


        // STEP 6.2: CREATE AUTHENTICATION REQUEST

        // This object contains the email and password submitted
        // by the user through the login form.
        //
        // At this stage, the authentication request is unauthenticated.
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.password()
                );


        // STEP 6.3: AUTHENTICATE USER

        // AuthenticationManager verifies the supplied credentials.
        //
        // Internally:
        // 1. CustomUserDetailsService fetches the user.
        // 2. PasswordEncoder verifies the password.
        // 3. Spring Security checks whether the account is enabled.
        //
        // If authentication fails, an AuthenticationException is thrown.
        authenticationManager.authenticate(authenticationToken);


        // STEP 6.4: FETCH AUTHENTICATED USER FROM DATABASE

        // Retrieve the complete User entity because JwtService
        // needs the user's ID, email and role to generate the JWT.
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Authenticated user could not be found"
                        )
                );


        // STEP 6.5: GENERATE JWT TOKEN

        // JwtService generates a signed token containing
        // the user's email, ID and role.
        String jwtToken = jwtService.generateToken(user);


        // STEP 6.6: CREATE USER RESPONSE

        // Prepare safe user information for the frontend.
        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );


        // STEP 6.7: RETURN LOGIN RESPONSE

        // Return JWT token along with the authenticated user's details.
        return new AuthResponse(
                jwtToken,
                "Bearer",
                userResponse
        );
    }


    // STEP 7: FETCH CURRENTLY AUTHENTICATED USER

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {

        // STEP 7.1: FIND USER USING EMAIL

        // Email is extracted from the authenticated JWT principal.
        // Fetch the latest user information from MySQL.
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User account not found"
                        )
                );


        // STEP 7.2: RETURN SAFE USER INFORMATION

        // Never expose the user's password or password hash.
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}

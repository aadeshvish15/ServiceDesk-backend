package com.servicedesk.servicedesk_backend.security;
import com.servicedesk.servicedesk_backend.user.User;
import com.servicedesk.servicedesk_backend.user.UserRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    // STEP 1: INJECT USERREPOSITORY
    // We need UserRepository to fetch user records from MySQL.
    private final UserRepository userRepository;

    // STEP 2: CONSTRUCTOR INJECTION
    // Spring automatically provides the UserRepository object.
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // STEP 3: IMPLEMENT SPRING SECURITY METHOD
    // Spring Security calls this method when it needs to
    // load a user using their username (our username is email).
    @Override
    public UserDetails loadUserByUsername(String email) {

        // STEP 4: FIND USER IN MYSQL USING EMAIL
        // If the user does not exist, throw UsernameNotFoundException.
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email
                        )
                );

        // STEP 5: CONVERT USER ROLE INTO SPRING SECURITY AUTHORITY
        // Spring Security expects roles in the format ROLE_ADMIN,
        // ROLE_AGENT or ROLE_EMPLOYEE.
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name()
                )
        );

        // STEP 6: CONVERT OUR USER ENTITY INTO USERDETAILS
        // UserDetails is the format understood by Spring Security.
        // We provide the email, password hash, authorities and
        // account enabled status.
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(!user.isEnabled())
                .build();
    }
}

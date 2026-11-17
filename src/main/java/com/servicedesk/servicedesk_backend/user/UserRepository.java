package com.servicedesk.servicedesk_backend.user;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    // STEP 1: FIND USER USING EMAIL
    // Spring Data JPA automatically generates the SQL query.
    // Optional handles the possibility that the user does not exist.
    Optional<User> findByEmailIgnoreCase(String email);

    // STEP 2: CHECK WHETHER EMAIL IS ALREADY REGISTERED
    // Returns true if a user already exists with this email.
    boolean existsByEmailIgnoreCase(String email);
// STEP: FIND ALL ACTIVE USERS WITH A PARTICULAR ROLE

    List<User> findByRoleAndEnabledTrue(Role role);
}

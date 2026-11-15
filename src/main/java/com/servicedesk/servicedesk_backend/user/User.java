package com.servicedesk.servicedesk_backend.user;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    // STEP 1: DEFINE THE PRIMARY KEY
    // IDENTITY tells MySQL to automatically generate the ID.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // STEP 2: STORE THE USER'S NAME
    @Column(nullable = false, length = 100)
    private String name;

    // STEP 3: STORE THE USER'S EMAIL
    // UNIQUE ensures two users cannot register with the same email.
    @Column(nullable = false, unique = true, length = 190)
    private String email;

    // STEP 4: STORE THE USER'S PASSWORD
    // IMPORTANT: We will store the BCrypt HASH, not the plain password.
    @Column(nullable = false)
    private String password;

    // STEP 5: STORE USER ROLE
    // EnumType.STRING stores ADMIN, AGENT or EMPLOYEE in MySQL.
    // We avoid ordinal numbers such as 0, 1 and 2.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // STEP 6: CHECK WHETHER THE ACCOUNT IS ACTIVE
    // Disabled users should not be allowed to log in.
    @Column(nullable = false)
    private boolean enabled = true;

    // STEP 7: STORE ACCOUNT CREATION DATE
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // STEP 8: DEFAULT CONSTRUCTOR
    // JPA/Hibernate requires a no-argument constructor
    // to create entity objects from database records.
    protected User() {
    }

    // STEP 9: PARAMETERIZED CONSTRUCTOR
    // Used when creating a new user from our service layer.
    public User(String name, String email, String password, Role role) {

        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = true;
    }

    // STEP 10: AUTOMATICALLY SET CREATION TIMESTAMP
    // This executes before a new User entity is inserted.
    @PrePersist
    protected void onCreate() {

        this.createdAt = LocalDateTime.now();
    }

    // STEP 11: GETTERS AND SETTERS
    // Used by our service layer, DTO mapping and JPA operations.

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

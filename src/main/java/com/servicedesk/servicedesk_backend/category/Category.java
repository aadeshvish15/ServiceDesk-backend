
package com.servicedesk.servicedesk_backend.category;

// STEP 1: IMPORT REQUIRED CLASSES

import jakarta.persistence.*;

import java.time.LocalDateTime;


// STEP 2: DECLARE CATEGORY ENTITY

@Entity
@Table(
        name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_categories_name",
                        columnNames = "name"
                )
        }
)
public class Category {

    // STEP 3: DEFINE PRIMARY KEY

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // STEP 4: DEFINE CATEGORY NAME

    // Every category must have a unique name.
    @Column(nullable = false, length = 100)
    private String name;


    // STEP 5: DEFINE CATEGORY DESCRIPTION

    @Column(length = 500)
    private String description;


    // STEP 6: DEFINE CATEGORY STATUS

    // Inactive categories cannot be selected for new tickets.
    // Existing tickets can still reference inactive categories.
    @Column(nullable = false)
    private boolean active = true;


    // STEP 7: DEFINE CREATION TIMESTAMP

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // STEP 8: DEFINE DEFAULT CONSTRUCTOR

    // Required by JPA/Hibernate.
    protected Category() {
    }


    // STEP 9: DEFINE PARAMETERIZED CONSTRUCTOR

    public Category(String name, String description) {

        this.name = name;
        this.description = description;
        this.active = true;
    }


    // STEP 10: SET CREATION TIMESTAMP

    // Executes automatically before inserting a new category.
    @PrePersist
    protected void onCreate() {

        this.createdAt = LocalDateTime.now();
    }


    // STEP 11: UPDATE CATEGORY DETAILS

    public void updateDetails(String name, String description) {

        this.name = name;
        this.description = description;
    }


    // STEP 12: UPDATE CATEGORY STATUS

    public void updateStatus(boolean active) {

        this.active = active;
    }


    // STEP 13: DEFINE GETTERS

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

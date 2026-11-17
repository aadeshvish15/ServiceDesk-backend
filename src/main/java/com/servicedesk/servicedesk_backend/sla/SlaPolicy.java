package com.servicedesk.servicedesk_backend.sla;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.ticket.TicketPriority;

import jakarta.persistence.*;

import java.time.LocalDateTime;


// STEP 2: DECLARE SLA POLICY ENTITY

@Entity
@Table(
        name = "sla_policies",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sla_policy_priority",
                        columnNames = "priority"
                )
        }
)
public class SlaPolicy {

    // STEP 3: DEFINE PRIMARY KEY

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // STEP 4: DEFINE TICKET PRIORITY

    // One policy exists for each priority.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, unique = true)
    private TicketPriority priority;


    // STEP 5: DEFINE FIRST RESPONSE TIME

    // Store duration in minutes.
    @Column(nullable = false)
    private Integer firstResponseMinutes;


    // STEP 6: DEFINE RESOLUTION TIME

    // Store duration in minutes.
    @Column(nullable = false)
    private Integer resolutionMinutes;


    // STEP 7: DEFINE POLICY STATUS

    @Column(nullable = false)
    private boolean active = true;


    // STEP 8: DEFINE CREATION TIMESTAMP

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // STEP 9: DEFINE UPDATE TIMESTAMP

    @Column(nullable = false)
    private LocalDateTime updatedAt;


    // STEP 10: DEFAULT CONSTRUCTOR

    // Required by JPA/Hibernate.
    protected SlaPolicy() {
    }


    // STEP 11: PARAMETERIZED CONSTRUCTOR

    public SlaPolicy(
            TicketPriority priority,
            Integer firstResponseMinutes,
            Integer resolutionMinutes
    ) {

        this.priority = priority;
        this.firstResponseMinutes = firstResponseMinutes;
        this.resolutionMinutes = resolutionMinutes;
        this.active = true;
    }


    // STEP 12: SET TIMESTAMPS BEFORE INSERTING

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }


    // STEP 13: UPDATE TIMESTAMP BEFORE UPDATING

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = LocalDateTime.now();
    }


    // STEP 14: UPDATE POLICY DETAILS

    public void updatePolicy(
            TicketPriority priority,
            Integer firstResponseMinutes,
            Integer resolutionMinutes
    ) {

        this.priority = priority;
        this.firstResponseMinutes = firstResponseMinutes;
        this.resolutionMinutes = resolutionMinutes;
    }


    // STEP 15: UPDATE POLICY STATUS

    public void updateStatus(boolean active) {

        this.active = active;
    }


    // STEP 16: DEFINE GETTERS

    public Long getId() {
        return id;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public Integer getFirstResponseMinutes() {
        return firstResponseMinutes;
    }

    public Integer getResolutionMinutes() {
        return resolutionMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}
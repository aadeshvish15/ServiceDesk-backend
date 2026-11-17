package com.servicedesk.servicedesk_backend.activity;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.ticket.Ticket;
import com.servicedesk.servicedesk_backend.user.User;

import jakarta.persistence.*;

import java.time.LocalDateTime;


// STEP 2: DECLARE TICKET ACTIVITY ENTITY

@Entity
@Table(
        name = "ticket_activities",
        indexes = {
                @Index(
                        name = "idx_activities_ticket_created",
                        columnList = "ticket_id, created_at"
                )
        }
)
public class TicketActivity {

    // STEP 3: DEFINE PRIMARY KEY

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // STEP 4: CONNECT ACTIVITY TO TICKET

    // One ticket can have multiple activity records.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;


    // STEP 5: CONNECT ACTIVITY TO PERFORMING USER

    // This stores who performed the action.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performed_by_id", nullable = false)
    private User performedBy;


    // STEP 6: STORE ACTIVITY ACTION

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ActivityAction action;


    // STEP 7: STORE ACTIVITY DESCRIPTION

    @Column(nullable = false, length = 1000)
    private String description;


    // STEP 8: STORE ACTIVITY VISIBILITY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityVisibility visibility;


    // STEP 9: STORE ACTIVITY CREATION TIME

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // STEP 10: DEFAULT CONSTRUCTOR

    // Required by JPA/Hibernate.
    protected TicketActivity() {
    }


    // STEP 11: PARAMETERIZED CONSTRUCTOR

    public TicketActivity(
            Ticket ticket,
            User performedBy,
            ActivityAction action,
            String description,
            ActivityVisibility visibility
    ) {

        this.ticket = ticket;
        this.performedBy = performedBy;
        this.action = action;
        this.description = description;
        this.visibility = visibility;
    }


    // STEP 12: SET TIMESTAMP BEFORE INSERTING

    @PrePersist
    protected void onCreate() {

        this.createdAt = LocalDateTime.now();
    }


    // STEP 13: DEFINE GETTERS

    public Long getId() {
        return id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public User getPerformedBy() {
        return performedBy;
    }

    public ActivityAction getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }

    public ActivityVisibility getVisibility() {
        return visibility;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
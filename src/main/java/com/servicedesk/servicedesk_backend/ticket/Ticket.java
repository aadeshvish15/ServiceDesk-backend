package com.servicedesk.servicedesk_backend.ticket;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.category.Category;
import com.servicedesk.servicedesk_backend.user.User;

import jakarta.persistence.*;

import java.time.LocalDateTime;


// STEP 2: DECLARE TICKET ENTITY

@Entity
@Table(
        name = "tickets",
        indexes = {
                @Index(
                        name = "idx_tickets_created_by",
                        columnList = "created_by_id"
                ),
                @Index(
                        name = "idx_tickets_assigned_to",
                        columnList = "assigned_to_id"
                ),
                @Index(
                        name = "idx_tickets_status_created_at",
                        columnList = "status, created_at"
                )
        }
)
public class Ticket {

    // STEP 3: DEFINE PRIMARY KEY

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // STEP 4: DEFINE TICKET SUBJECT

    // Short title describing the user's issue.
    @Column(nullable = false, length = 150)
    private String subject;


    // STEP 5: DEFINE TICKET DESCRIPTION

    // TEXT allows the user to explain the issue in detail.
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;


    // STEP 6: DEFINE TICKET PRIORITY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;


    // STEP 7: DEFINE TICKET STATUS

    // Newly created tickets will initially have OPEN status.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.OPEN;


    // STEP 8: CONNECT TICKET WITH CATEGORY

    // Many tickets can belong to one category.
    // Example: Multiple tickets can belong to Hardware.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;


    // STEP 9: CONNECT TICKET WITH THE EMPLOYEE WHO CREATED IT

    // Many tickets can be created by the same employee.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;


    // STEP 10: CONNECT TICKET WITH THE ASSIGNED AGENT

    // A ticket can remain unassigned initially.
    // Admin can assign it to an agent later.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;


    // STEP 11: DEFINE CREATION TIMESTAMP

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // STEP 12: DEFINE LAST UPDATE TIMESTAMP

    @Column(nullable = false)
    private LocalDateTime updatedAt;


    // STEP 13: DEFINE RESOLUTION TIMESTAMP

    // This remains null until the ticket is resolved.
    private LocalDateTime resolvedAt;


    // STEP 14: DEFAULT CONSTRUCTOR

    // Required by JPA/Hibernate.
    protected Ticket() {
    }


    // STEP 15: PARAMETERIZED CONSTRUCTOR

    // Used by TicketService when an employee creates a ticket.
    public Ticket(
            String subject,
            String description,
            TicketPriority priority,
            Category category,
            User createdBy
    ) {

        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.category = category;
        this.createdBy = createdBy;

        // Newly created ticket starts with OPEN status.
        this.status = TicketStatus.OPEN;

        // A new ticket is not assigned to an agent yet.
        this.assignedTo = null;
    }


    // STEP 16: SET TIMESTAMPS BEFORE INSERTING A NEW TICKET

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }


    // STEP 17: UPDATE TIMESTAMP BEFORE UPDATING A TICKET

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = LocalDateTime.now();
    }


    // STEP 18: ASSIGN TICKET TO AN AGENT

    public void assignTo(User agent) {

        this.assignedTo = agent;
    }


    // STEP 19: UPDATE TICKET STATUS

    public void updateStatus(TicketStatus nextStatus) {

        // Prevent null status updates.
        if (nextStatus == null) {
            throw new IllegalArgumentException(
                    "Ticket status cannot be null"
            );
        }

        // Check whether the requested status transition is allowed.
        boolean validTransition = switch (this.status) {

            case OPEN ->
                    nextStatus == TicketStatus.IN_PROGRESS
                            || nextStatus == TicketStatus.PENDING;

            case IN_PROGRESS ->
                    nextStatus == TicketStatus.PENDING
                            || nextStatus == TicketStatus.RESOLVED;

            case PENDING ->
                    nextStatus == TicketStatus.IN_PROGRESS
                            || nextStatus == TicketStatus.RESOLVED;

            case RESOLVED ->
                    nextStatus == TicketStatus.CLOSED
                            || nextStatus == TicketStatus.IN_PROGRESS;

            case CLOSED -> false;
        };

        // Reject invalid status transitions.
        if (!validTransition) {

            throw new IllegalArgumentException(
                    "Cannot change ticket status from "
                            + this.status
                            + " to "
                            + nextStatus
            );
        }

        // Update the actual ticket status.
        this.status = nextStatus;

        // Store resolution timestamp when ticket gets resolved.
        if (nextStatus == TicketStatus.RESOLVED) {

            this.resolvedAt = LocalDateTime.now();
        }

        // Clear resolution timestamp when a resolved ticket is reopened.
        if (nextStatus == TicketStatus.IN_PROGRESS) {

            this.resolvedAt = null;
        }
    }


    // STEP 20: DEFINE GETTERS

    public Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public Category getCategory() {
        return category;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

}
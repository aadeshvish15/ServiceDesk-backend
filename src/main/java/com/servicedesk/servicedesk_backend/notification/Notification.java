package com.servicedesk.servicedesk_backend.notification;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.ticket.Ticket;
import com.servicedesk.servicedesk_backend.user.User;

import jakarta.persistence.*;

import java.time.LocalDateTime;


// STEP 2: DECLARE NOTIFICATION ENTITY

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(
                        name = "idx_notifications_recipient_created",
                        columnList = "recipient_id, created_at"
                ),
                @Index(
                        name = "idx_notifications_recipient_read",
                        columnList = "recipient_id, read_at"
                )
        }
)
public class Notification {

    // STEP 3: DEFINE PRIMARY KEY

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // STEP 4: DEFINE NOTIFICATION RECIPIENT

    // Every notification belongs to one user.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;


    // STEP 5: DEFINE RELATED TICKET

    // The notification is associated with a specific ticket.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;


    // STEP 6: DEFINE NOTIFICATION TYPE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;


    // STEP 7: DEFINE NOTIFICATION TITLE

    @Column(nullable = false, length = 120)
    private String title;


    // STEP 8: DEFINE NOTIFICATION MESSAGE

    @Column(nullable = false, length = 500)
    private String message;


    // STEP 9: DEFINE CREATION TIMESTAMP

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // STEP 10: DEFINE READ TIMESTAMP

    // NULL means the notification has not been read.
    private LocalDateTime readAt;


    // STEP 11: DEFAULT CONSTRUCTOR

    // Required by JPA/Hibernate.
    protected Notification() {
    }


    // STEP 12: PARAMETERIZED CONSTRUCTOR

    public Notification(
            User recipient,
            Ticket ticket,
            NotificationType type,
            String title,
            String message
    ) {

        this.recipient = recipient;
        this.ticket = ticket;
        this.type = type;
        this.title = title;
        this.message = message;
    }


    // STEP 13: SET CREATION TIMESTAMP

    @PrePersist
    protected void onCreate() {

        this.createdAt = LocalDateTime.now();
    }


    // STEP 14: MARK NOTIFICATION AS READ

    public void markAsRead() {

        // Don't overwrite the original read timestamp.
        if (this.readAt == null) {

            this.readAt = LocalDateTime.now();
        }
    }


    // STEP 15: DEFINE GETTERS

    public Long getId() {
        return id;
    }

    public User getRecipient() {
        return recipient;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public boolean isRead() {
        return readAt != null;
    }
}
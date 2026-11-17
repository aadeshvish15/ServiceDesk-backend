package com.servicedesk.servicedesk_backend.comment;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.ticket.Ticket;
import com.servicedesk.servicedesk_backend.user.User;

import jakarta.persistence.*;

import java.time.LocalDateTime;


// STEP 2: DECLARE COMMENT ENTITY

@Entity
@Table(
        name = "ticket_comments",
        indexes = {
                @Index(
                        name = "idx_comments_ticket_created",
                        columnList = "ticket_id, created_at"
                )
        }
)
public class Comment {

    // STEP 3: DEFINE PRIMARY KEY

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // STEP 4: CONNECT COMMENT TO TICKET

    // One ticket can have multiple comments.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;


    // STEP 5: CONNECT COMMENT TO AUTHOR

    // One user can write multiple comments.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;


    // STEP 6: STORE COMMENT CONTENT

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;


    // STEP 7: DEFINE COMMENT VISIBILITY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentVisibility visibility;


    // STEP 8: STORE COMMENT CREATION TIME

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // STEP 9: DEFAULT CONSTRUCTOR

    // Required by JPA/Hibernate.
    protected Comment() {
    }


    // STEP 10: PARAMETERIZED CONSTRUCTOR

    public Comment(
            Ticket ticket,
            User author,
            String content,
            CommentVisibility visibility
    ) {

        this.ticket = ticket;
        this.author = author;
        this.content = content;
        this.visibility = visibility;
    }


    // STEP 11: SET TIMESTAMP BEFORE INSERTING

    @PrePersist
    protected void onCreate() {

        this.createdAt = LocalDateTime.now();
    }


    // STEP 12: DEFINE GETTERS

    public Long getId() {
        return id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public User getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public CommentVisibility getVisibility() {
        return visibility;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
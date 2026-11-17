package com.servicedesk.servicedesk_backend.comment;

// STEP 1: IMPORT REQUIRED CLASSES

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


// STEP 2: DECLARE COMMENT REPOSITORY

public interface CommentRepository extends JpaRepository<Comment, Long> {


    // STEP 3: FETCH ALL COMMENTS FOR A TICKET

    // Used by agents and admins.
    // Oldest comments appear first.
    List<Comment> findByTicket_IdOrderByCreatedAtAsc(Long ticketId);


    // STEP 4: FETCH ONLY PUBLIC COMMENTS

    // Used by employees.
    // Internal notes will never be returned by this query.
    List<Comment> findByTicket_IdAndVisibilityOrderByCreatedAtAsc(
            Long ticketId,
            CommentVisibility visibility
    );

}
package com.servicedesk.servicedesk_backend.activity;

// STEP 1: IMPORT REQUIRED CLASSES

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


// STEP 2: DECLARE ACTIVITY REPOSITORY

public interface TicketActivityRepository
        extends JpaRepository<TicketActivity, Long> {


    // STEP 3: FETCH ALL ACTIVITIES FOR A TICKET

    // Used by agents and admins.
    List<TicketActivity> findByTicket_IdOrderByCreatedAtAsc(
            Long ticketId
    );


    // STEP 4: FETCH ONLY PUBLIC ACTIVITIES

    // Used by employees.
    // Internal notes and their events are excluded.
    List<TicketActivity> findByTicket_IdAndVisibilityOrderByCreatedAtAsc(
            Long ticketId,
            ActivityVisibility visibility
    );

}
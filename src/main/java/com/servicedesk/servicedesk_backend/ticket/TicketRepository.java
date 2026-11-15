package com.servicedesk.servicedesk_backend.ticket;

// STEP 1: IMPORT REQUIRED CLASSES

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


// STEP 2: DECLARE TICKET REPOSITORY

// JpaRepository provides built-in CRUD database operations.
public interface TicketRepository extends JpaRepository<Ticket, Long> {


    // STEP 3: FETCH ALL TICKETS

    // Admin and agents can use this method to view the ticket list.
    // Newest tickets appear first.
    List<Ticket> findAllByOrderByCreatedAtDesc();


    // STEP 4: FETCH TICKETS CREATED BY A PARTICULAR EMPLOYEE

    // Used when an employee views their own tickets.
    List<Ticket> findByCreatedBy_IdOrderByCreatedAtDesc(Long userId);


    // STEP 5: FETCH TICKETS ASSIGNED TO A PARTICULAR AGENT

    List<Ticket> findByAssignedTo_IdOrderByCreatedAtDesc(Long agentId);


    // STEP 6: FETCH UNASSIGNED TICKETS

    // Agents can see tickets that have not yet been assigned.
    List<Ticket> findByAssignedToIsNullOrderByCreatedAtDesc();

}
package com.servicedesk.servicedesk_backend.sla;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.ticket.TicketPriority;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


// STEP 2: DECLARE SLA POLICY REPOSITORY

public interface SlaPolicyRepository
        extends JpaRepository<SlaPolicy, Long> {


    // STEP 3: FIND POLICY USING PRIORITY

    Optional<SlaPolicy> findByPriority(TicketPriority priority);


    // STEP 4: FIND ACTIVE POLICY USING PRIORITY

    Optional<SlaPolicy> findByPriorityAndActiveTrue(
            TicketPriority priority
    );


    // STEP 5: CHECK WHETHER POLICY ALREADY EXISTS

    boolean existsByPriority(TicketPriority priority);


    // STEP 6: CHECK DUPLICATE PRIORITY DURING UPDATE

    boolean existsByPriorityAndIdNot(
            TicketPriority priority,
            Long id
    );


    // STEP 7: FETCH ALL POLICIES

    List<SlaPolicy> findAllByOrderByPriorityAsc();

}
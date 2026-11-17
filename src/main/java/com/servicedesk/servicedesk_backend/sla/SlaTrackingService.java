package com.servicedesk.servicedesk_backend.sla;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.category.exception.ResourceNotFoundException;

import com.servicedesk.servicedesk_backend.sla.dto.SlaStatusResponse;

import com.servicedesk.servicedesk_backend.ticket.Ticket;
import com.servicedesk.servicedesk_backend.ticket.TicketRepository;

import com.servicedesk.servicedesk_backend.user.Role;
import com.servicedesk.servicedesk_backend.user.User;
import com.servicedesk.servicedesk_backend.user.UserRepository;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


// STEP 2: DECLARE SLA TRACKING SERVICE

@Service
@Transactional
public class SlaTrackingService {

    // STEP 3: INJECT REQUIRED REPOSITORIES

    private final TicketRepository ticketRepository;

    private final UserRepository userRepository;

    private final SlaPolicyRepository slaPolicyRepository;


    // STEP 4: CONSTRUCTOR INJECTION

    public SlaTrackingService(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            SlaPolicyRepository slaPolicyRepository
    ) {

        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.slaPolicyRepository = slaPolicyRepository;
    }


    // STEP 5: FETCH SLA DETAILS FOR EMPLOYEE

    public SlaStatusResponse getEmployeeSla(
            Long ticketId,
            String employeeEmail
    ) {

        // STEP 5.1: FIND LOGGED-IN EMPLOYEE
        User employee = findEnabledUser(employeeEmail);


        // STEP 5.2: FIND TICKET
        Ticket ticket = findTicket(ticketId);


        // STEP 5.3: VERIFY TICKET OWNERSHIP
        if (!ticket.getCreatedBy().getId().equals(employee.getId())) {

            throw new AccessDeniedException(
                    "You cannot view SLA information for another employee's ticket"
            );
        }


        // STEP 5.4: CALCULATE SLA STATUS
        return calculateSlaStatus(ticket);
    }


    // STEP 6: FETCH SLA DETAILS FOR AGENT OR ADMIN

    public SlaStatusResponse getAgentSla(
            Long ticketId,
            String currentUserEmail,
            boolean adminOverride
    ) {

        // STEP 6.1: FIND LOGGED-IN USER
        User currentUser = findEnabledUser(currentUserEmail);


        // STEP 6.2: FIND TICKET
        Ticket ticket = findTicket(ticketId);


        // STEP 6.3: VERIFY AGENT OR ADMIN ACCESS
        verifyAgentAccess(
                ticket,
                currentUser,
                adminOverride
        );


        // STEP 6.4: CALCULATE SLA STATUS
        return calculateSlaStatus(ticket);
    }


    // STEP 7: CALCULATE SLA STATUS

    private SlaStatusResponse calculateSlaStatus(Ticket ticket) {

        // STEP 7.1: ENSURE TICKET HAS SLA DEADLINES

        // Older tickets may not have deadlines because they were
        // created before the SLA module was implemented.
        ensureSlaDeadlines(ticket);


        // STEP 7.2: GET CURRENT TIME
        LocalDateTime evaluatedAt = LocalDateTime.now();


        // STEP 7.3: GET FIRST RESPONSE DEADLINE
        LocalDateTime firstResponseDueAt =
                ticket.getFirstResponseDueAt();


        // STEP 7.4: GET ACTUAL FIRST RESPONSE TIME
        LocalDateTime firstRespondedAt =
                ticket.getFirstRespondedAt();


        // STEP 7.5: CHECK WHETHER FIRST RESPONSE WAS MET
        boolean firstResponseMet =
                firstRespondedAt != null
                        && !firstRespondedAt.isAfter(firstResponseDueAt);


        // STEP 7.6: CHECK WHETHER FIRST RESPONSE WAS BREACHED

        // If a response exists, compare its actual time with the deadline.
        // Otherwise, compare the current time with the deadline.
        boolean firstResponseBreached;

        if (firstRespondedAt != null) {

            firstResponseBreached =
                    firstRespondedAt.isAfter(firstResponseDueAt);

        } else {

            firstResponseBreached =
                    evaluatedAt.isAfter(firstResponseDueAt);
        }


        // STEP 7.7: GET RESOLUTION DEADLINE
        LocalDateTime resolutionDueAt =
                ticket.getResolutionDueAt();


        // STEP 7.8: GET ACTUAL RESOLUTION TIME
        LocalDateTime resolvedAt =
                ticket.getResolvedAt();


        // STEP 7.9: CHECK WHETHER RESOLUTION WAS MET
        boolean resolutionMet =
                resolvedAt != null
                        && !resolvedAt.isAfter(resolutionDueAt);


        // STEP 7.10: CHECK WHETHER RESOLUTION WAS BREACHED

        // A resolved ticket is checked using resolvedAt.
        // An unresolved ticket is checked using the current time.
        boolean resolutionBreached;

        if (resolvedAt != null) {

            resolutionBreached =
                    resolvedAt.isAfter(resolutionDueAt);

        } else {

            resolutionBreached =
                    evaluatedAt.isAfter(resolutionDueAt);
        }


        // STEP 7.11: CREATE SLA RESPONSE DTO

        return new SlaStatusResponse(

                ticket.getId(),

                ticket.getPriority(),

                ticket.getStatus(),

                firstResponseDueAt,

                firstRespondedAt,

                firstResponseMet,

                firstResponseBreached,

                resolutionDueAt,

                resolvedAt,

                resolutionMet,

                resolutionBreached,

                evaluatedAt
        );
    }


    // STEP 8: BACKFILL SLA DEADLINES FOR EXISTING TICKETS

    private void ensureSlaDeadlines(Ticket ticket) {

        // STEP 8.1: CHECK WHETHER DEADLINES ALREADY EXIST
        if (
                ticket.getFirstResponseDueAt() != null
                        && ticket.getResolutionDueAt() != null
        ) {
            return;
        }


        // STEP 8.2: FIND POLICY FOR THE TICKET'S PRIORITY

        // Use the existing policy even if it is now inactive.
        // We don't want deactivating a policy to erase old ticket deadlines.
        SlaPolicy policy = slaPolicyRepository
                .findByPriority(ticket.getPriority())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SLA policy not found for priority: "
                                + ticket.getPriority()
                ));


        // STEP 8.3: GET ORIGINAL TICKET CREATION TIME
        LocalDateTime createdAt = ticket.getCreatedAt();


        // STEP 8.4: CALCULATE FIRST RESPONSE DEADLINE
        LocalDateTime firstResponseDueAt = createdAt.plusMinutes(
                policy.getFirstResponseMinutes()
        );


        // STEP 8.5: CALCULATE RESOLUTION DEADLINE
        LocalDateTime resolutionDueAt = createdAt.plusMinutes(
                policy.getResolutionMinutes()
        );


        // STEP 8.6: STORE CALCULATED DEADLINES
        ticket.setSlaDeadlines(
                firstResponseDueAt,
                resolutionDueAt
        );


        // STEP 8.7: SAVE UPDATED TICKET
        ticketRepository.save(ticket);
    }


    // STEP 9: VERIFY AGENT OR ADMIN ACCESS

    private void verifyAgentAccess(
            Ticket ticket,
            User currentUser,
            boolean adminOverride
    ) {

        // STEP 9.1: VERIFY ADMIN OVERRIDE
        if (adminOverride) {

            if (currentUser.getRole() != Role.ADMIN) {

                throw new AccessDeniedException(
                        "Admin permission is required"
                );
            }

            return;
        }


        // STEP 9.2: VERIFY AGENT ROLE
        if (currentUser.getRole() != Role.AGENT) {

            throw new AccessDeniedException(
                    "Only assigned agents or admins can view this SLA"
            );
        }


        // STEP 9.3: CHECK AGENT ASSIGNMENT
        boolean assignedToCurrentAgent =
                ticket.getAssignedTo() != null
                        && ticket.getAssignedTo()
                        .getId()
                        .equals(currentUser.getId());


        // STEP 9.4: REJECT UNAUTHORIZED AGENT
        if (!assignedToCurrentAgent) {

            throw new AccessDeniedException(
                    "You can only view SLA details for tickets assigned to you"
            );
        }
    }


    // STEP 10: FIND ENABLED USER BY EMAIL

    private User findEnabledUser(String email) {

        return userRepository
                .findByEmailIgnoreCase(email)
                .filter(User::isEnabled)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active user not found"
                ));
    }


    // STEP 11: FIND TICKET BY ID

    private Ticket findTicket(Long ticketId) {

        return ticketRepository
                .findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found with ID: " + ticketId
                ));
    }

}
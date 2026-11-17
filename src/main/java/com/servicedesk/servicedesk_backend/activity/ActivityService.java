package com.servicedesk.servicedesk_backend.activity;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.activity.dto.ActivityResponse;

import com.servicedesk.servicedesk_backend.category.exception.ResourceNotFoundException;

import com.servicedesk.servicedesk_backend.ticket.Ticket;
import com.servicedesk.servicedesk_backend.ticket.TicketRepository;

import com.servicedesk.servicedesk_backend.user.Role;
import com.servicedesk.servicedesk_backend.user.User;
import com.servicedesk.servicedesk_backend.user.UserRepository;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


// STEP 2: DECLARE ACTIVITY SERVICE

@Service
@Transactional
public class ActivityService {

    // STEP 3: DECLARE REQUIRED DEPENDENCIES

    private final TicketActivityRepository activityRepository;

    private final TicketRepository ticketRepository;

    private final UserRepository userRepository;


    // STEP 4: CONSTRUCTOR INJECTION

    public ActivityService(
            TicketActivityRepository activityRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository
    ) {

        this.activityRepository = activityRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }


    // STEP 5: RECORD A NEW ACTIVITY EVENT

    // Other services call this method whenever a ticket event occurs.
    public void recordActivity(
            Ticket ticket,
            User performedBy,
            ActivityAction action,
            String description,
            ActivityVisibility visibility
    ) {

        // STEP 5.1: CREATE ACTIVITY OBJECT
        TicketActivity activity = new TicketActivity(
                ticket,
                performedBy,
                action,
                description,
                visibility
        );


        // STEP 5.2: SAVE ACTIVITY IN DATABASE
        activityRepository.save(activity);
    }


    // STEP 6: FETCH EMPLOYEE ACTIVITY HISTORY

    @Transactional(readOnly = true)
    public List<ActivityResponse> getEmployeeActivities(
            Long ticketId,
            String employeeEmail
    ) {

        // STEP 6.1: FIND LOGGED-IN EMPLOYEE
        User employee = findEnabledUser(employeeEmail);


        // STEP 6.2: FIND TICKET
        Ticket ticket = findTicket(ticketId);


        // STEP 6.3: VERIFY TICKET OWNERSHIP
        if (!ticket.getCreatedBy().getId().equals(employee.getId())) {

            throw new AccessDeniedException(
                    "You cannot view another employee's ticket activity"
            );
        }


        // STEP 6.4: FETCH ONLY PUBLIC ACTIVITIES

        List<TicketActivity> activities = activityRepository
                .findByTicket_IdAndVisibilityOrderByCreatedAtAsc(
                        ticketId,
                        ActivityVisibility.PUBLIC
                );


        // STEP 6.5: CONVERT ACTIVITIES INTO DTOs
        return activities.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // STEP 7: FETCH AGENT OR ADMIN ACTIVITY HISTORY

    @Transactional(readOnly = true)
    public List<ActivityResponse> getAgentActivities(
            Long ticketId,
            String currentUserEmail,
            boolean adminOverride
    ) {

        // STEP 7.1: FIND LOGGED-IN USER
        User currentUser = findEnabledUser(currentUserEmail);


        // STEP 7.2: FIND TICKET
        Ticket ticket = findTicket(ticketId);


        // STEP 7.3: VERIFY AGENT OR ADMIN ACCESS
        verifyAgentAccess(
                ticket,
                currentUser,
                adminOverride
        );


        // STEP 7.4: FETCH ALL ACTIVITIES

        // This includes both PUBLIC and INTERNAL activities.
        List<TicketActivity> activities = activityRepository
                .findByTicket_IdOrderByCreatedAtAsc(ticketId);


        // STEP 7.5: CONVERT ACTIVITIES INTO DTOs
        return activities.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // STEP 8: VERIFY AGENT OR ADMIN ACCESS

    private void verifyAgentAccess(
            Ticket ticket,
            User currentUser,
            boolean adminOverride
    ) {

        // STEP 8.1: VERIFY ADMIN OVERRIDE

        if (adminOverride) {

            if (currentUser.getRole() != Role.ADMIN) {

                throw new AccessDeniedException(
                        "Admin permission is required"
                );
            }

            return;
        }


        // STEP 8.2: VERIFY AGENT ROLE

        if (currentUser.getRole() != Role.AGENT) {

            throw new AccessDeniedException(
                    "Only assigned agents or admins can access this activity"
            );
        }


        // STEP 8.3: VERIFY TICKET ASSIGNMENT

        boolean assignedToCurrentAgent =
                ticket.getAssignedTo() != null
                        && ticket.getAssignedTo()
                        .getId()
                        .equals(currentUser.getId());


        // STEP 8.4: REJECT UNAUTHORIZED ACCESS

        if (!assignedToCurrentAgent) {

            throw new AccessDeniedException(
                    "You can only view activity for tickets assigned to you"
            );
        }
    }


    // STEP 9: FIND ACTIVE USER BY EMAIL

    private User findEnabledUser(String email) {

        return userRepository
                .findByEmailIgnoreCase(email)
                .filter(User::isEnabled)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active user not found"
                ));
    }


    // STEP 10: FIND TICKET BY ID

    private Ticket findTicket(Long ticketId) {

        return ticketRepository
                .findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found with ID: " + ticketId
                ));
    }


    // STEP 11: CONVERT ACTIVITY ENTITY INTO RESPONSE DTO

    private ActivityResponse convertToResponse(
            TicketActivity activity
    ) {

        // STEP 11.1: GET USER WHO PERFORMED THE ACTIVITY
        User performedBy = activity.getPerformedBy();


        // STEP 11.2: CREATE RESPONSE DTO
        return new ActivityResponse(

                activity.getId(),

                activity.getTicket().getId(),

                activity.getAction(),

                activity.getDescription(),

                activity.getVisibility(),

                performedBy.getId(),

                performedBy.getName(),

                performedBy.getRole(),

                activity.getCreatedAt()
        );
    }

}
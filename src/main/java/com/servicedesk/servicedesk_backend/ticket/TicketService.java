package com.servicedesk.servicedesk_backend.ticket;

// STEP 1: IMPORT REQUIRED CLASSES
import com.servicedesk.servicedesk_backend.activity.ActivityAction;
import com.servicedesk.servicedesk_backend.activity.ActivityService;
import com.servicedesk.servicedesk_backend.activity.ActivityVisibility;

import com.servicedesk.servicedesk_backend.category.Category;
import com.servicedesk.servicedesk_backend.category.CategoryRepository;
import com.servicedesk.servicedesk_backend.category.exception.ResourceNotFoundException;

import com.servicedesk.servicedesk_backend.ticket.dto.TicketAssignRequest;
import com.servicedesk.servicedesk_backend.ticket.dto.TicketCreateRequest;
import com.servicedesk.servicedesk_backend.ticket.dto.TicketResponse;
import com.servicedesk.servicedesk_backend.ticket.dto.TicketStatusUpdateRequest;

import com.servicedesk.servicedesk_backend.user.Role;
import com.servicedesk.servicedesk_backend.user.User;
import com.servicedesk.servicedesk_backend.user.UserRepository;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


// STEP 2: DECLARE TICKET SERVICE

@Service
@Transactional
public class TicketService {

    // STEP 3: INJECT REQUIRED REPOSITORIES
    private final ActivityService activityService;
    private final TicketRepository ticketRepository;

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;


    // STEP 4: CONSTRUCTOR INJECTION

    public TicketService(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            ActivityService activityService
    ) {

        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.activityService = activityService;
    }


    // STEP 5: CREATE NEW TICKET

    public TicketResponse createTicket(
            String employeeEmail,
            TicketCreateRequest request
    ) {

        // STEP 5.1: FIND THE EMPLOYEE USING EMAIL

        User employee = findEnabledUserByEmail(employeeEmail);


        // STEP 5.2: FIND THE SELECTED CATEGORY

        // Only active categories can be selected for new tickets.
        Category category = categoryRepository
                .findById(request.categoryId())
                .filter(Category::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active category not found with ID: "
                                + request.categoryId()
                ));


        // STEP 5.3: CREATE TICKET OBJECT

        Ticket ticket = new Ticket(
                request.subject().trim(),
                request.description().trim(),
                request.priority(),
                category,
                employee
        );


        // STEP 5.4: SAVE TICKET IN DATABASE

        Ticket savedTicket = ticketRepository.save(ticket);
        // STEP: RECORD TICKET CREATION ACTIVITY

        activityService.recordActivity(
                savedTicket,
                employee,
                ActivityAction.TICKET_CREATED,
                "Ticket created",
                ActivityVisibility.PUBLIC
        );

        // STEP 5.5: CONVERT ENTITY INTO RESPONSE DTO

        return convertToResponse(savedTicket);
    }


    // STEP 6: FETCH ALL TICKETS CREATED BY EMPLOYEE

    @Transactional(readOnly = true)
    public List<TicketResponse> getEmployeeTickets(String employeeEmail) {

        // STEP 6.1: FIND LOGGED-IN EMPLOYEE
        User employee = findEnabledUserByEmail(employeeEmail);


        // STEP 6.2: FETCH EMPLOYEE'S TICKETS
        List<Ticket> tickets = ticketRepository
                .findByCreatedBy_IdOrderByCreatedAtDesc(employee.getId());


        // STEP 6.3: CONVERT EACH TICKET TO RESPONSE DTO
        return tickets.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // STEP 7: FETCH A SINGLE EMPLOYEE TICKET

    @Transactional(readOnly = true)
    public TicketResponse getEmployeeTicket(
            Long ticketId,
            String employeeEmail
    ) {

        // STEP 7.1: FIND LOGGED-IN EMPLOYEE
        User employee = findEnabledUserByEmail(employeeEmail);


        // STEP 7.2: FIND TICKET USING ID
        Ticket ticket = findTicketById(ticketId);


        // STEP 7.3: CHECK TICKET OWNERSHIP

        // An employee must not access another employee's ticket.
        if (!ticket.getCreatedBy().getId().equals(employee.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to access this ticket"
            );
        }


        // STEP 7.4: RETURN TICKET RESPONSE
        return convertToResponse(ticket);
    }


    // STEP 8: FETCH ALL TICKETS FOR ADMIN AND AGENTS

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {

        // STEP 8.1: FETCH ALL TICKETS
        List<Ticket> tickets = ticketRepository
                .findAllByOrderByCreatedAtDesc();


        // STEP 8.2: CONVERT ENTITIES INTO RESPONSE DTOs
        return tickets.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // STEP 9: FETCH UNASSIGNED TICKETS

    @Transactional(readOnly = true)
    public List<TicketResponse> getUnassignedTickets() {

        // STEP 9.1: FETCH TICKETS WITHOUT AN ASSIGNED AGENT
        List<Ticket> tickets = ticketRepository
                .findByAssignedToIsNullOrderByCreatedAtDesc();


        // STEP 9.2: CONVERT ENTITIES INTO RESPONSE DTOs
        return tickets.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // STEP 10: FETCH A SINGLE TICKET FOR ADMIN OR AGENT

    @Transactional(readOnly = true)
    public TicketResponse getTicket(Long ticketId) {

        // STEP 10.1: FIND TICKET
        Ticket ticket = findTicketById(ticketId);


        // STEP 10.2: RETURN RESPONSE DTO
        return convertToResponse(ticket);
    }


    // STEP 11: ASSIGN TICKET TO AN AGENT

// STEP: ASSIGN TICKET TO AGENT AND RECORD ACTIVITY

    public TicketResponse assignTicket(
            Long ticketId,
            String adminEmail,
            TicketAssignRequest request
    ) {

        // STEP 1: FIND ADMIN WHO IS PERFORMING THE ASSIGNMENT
        User admin = findEnabledUserByEmail(adminEmail);


        // STEP 2: VERIFY ADMIN ROLE
        if (admin.getRole() != Role.ADMIN) {

            throw new AccessDeniedException(
                    "Only admins can assign tickets"
            );
        }


        // STEP 3: FIND TICKET
        Ticket ticket = findTicketById(ticketId);


        // STEP 4: FIND REQUESTED AGENT
        User agent = userRepository
                .findById(request.agentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Agent not found with ID: " + request.agentId()
                ));


        // STEP 5: VERIFY AGENT ROLE AND ACCOUNT STATUS
        if (agent.getRole() != Role.AGENT || !agent.isEnabled()) {

            throw new IllegalArgumentException(
                    "Selected user is not an active agent"
            );
        }


        // STEP 6: STORE PREVIOUS ASSIGNED AGENT
        User previousAgent = ticket.getAssignedTo();


        // STEP 7: ASSIGN TICKET
        ticket.assignTo(agent);


        // STEP 8: SAVE UPDATED TICKET
        Ticket updatedTicket = ticketRepository.save(ticket);


        // STEP 9: PREPARE ACTIVITY DESCRIPTION
        String description;

        if (previousAgent == null) {

            description = "Ticket assigned to " + agent.getName();

        } else {

            description = "Ticket reassigned from "
                    + previousAgent.getName()
                    + " to "
                    + agent.getName();
        }


        // STEP 10: RECORD ASSIGNMENT ACTIVITY
        activityService.recordActivity(
                updatedTicket,
                admin,
                ActivityAction.TICKET_ASSIGNED,
                description,
                ActivityVisibility.PUBLIC
        );


        // STEP 11: RETURN UPDATED TICKET
        return convertToResponse(updatedTicket);
    }


    // STEP 12: UPDATE TICKET STATUS

    public TicketResponse updateTicketStatus(
            Long ticketId,
            String currentUserEmail,
            TicketStatusUpdateRequest request,
            boolean adminOverride
    ) {

        // STEP 12.1: FIND THE LOGGED-IN USER
        User currentUser = findEnabledUserByEmail(currentUserEmail);


        // STEP 12.2: FIND TICKET
        Ticket ticket = findTicketById(ticketId);


        // STEP 12.3: VERIFY AGENT ASSIGNMENT

        // An agent can only update tickets assigned to that agent.
        // Admin can update any ticket because adminOverride is true.
        if (!adminOverride) {

            boolean assignedToCurrentAgent =
                    ticket.getAssignedTo() != null
                            && ticket.getAssignedTo()
                            .getId()
                            .equals(currentUser.getId());

            if (!assignedToCurrentAgent) {

                throw new AccessDeniedException(
                        "You can only update tickets assigned to you"
                );
            }
        }


        // STEP 12.4: UPDATE TICKET STATUS

        // Entity checks whether the status transition is valid.
        ticket.updateStatus(request.status());


        // STEP 12.5: SAVE UPDATED TICKET
        Ticket updatedTicket = ticketRepository.save(ticket);


        // STEP 12.6: RETURN UPDATED TICKET
        return convertToResponse(updatedTicket);
    }


    // STEP 13: REUSABLE METHOD TO FIND ACTIVE USER BY EMAIL

    private User findEnabledUserByEmail(String email) {

        return userRepository
                .findByEmailIgnoreCase(email)
                .filter(User::isEnabled)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active user not found"
                ));
    }


    // STEP 14: REUSABLE METHOD TO FIND TICKET BY ID

    private Ticket findTicketById(Long ticketId) {

        return ticketRepository
                .findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found with ID: " + ticketId
                ));
    }


    // STEP 15: CONVERT TICKET ENTITY INTO RESPONSE DTO

    private TicketResponse convertToResponse(Ticket ticket) {

        // STEP 15.1: GET CATEGORY DETAILS
        Category category = ticket.getCategory();


        // STEP 15.2: GET EMPLOYEE DETAILS
        User employee = ticket.getCreatedBy();


        // STEP 15.3: GET ASSIGNED AGENT DETAILS
        User agent = ticket.getAssignedTo();


        // STEP 15.4: CREATE RESPONSE DTO

        return new TicketResponse(

                ticket.getId(),

                ticket.getSubject(),

                ticket.getDescription(),

                ticket.getPriority(),

                ticket.getStatus(),

                category.getId(),

                category.getName(),

                employee.getId(),

                employee.getName(),

                employee.getEmail(),

                agent != null ? agent.getId() : null,

                agent != null ? agent.getName() : null,

                agent != null ? agent.getEmail() : null,

                ticket.getCreatedAt(),

                ticket.getUpdatedAt(),

                ticket.getResolvedAt()
        );
    }

}
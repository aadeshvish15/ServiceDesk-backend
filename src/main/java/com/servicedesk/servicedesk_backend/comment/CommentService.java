package com.servicedesk.servicedesk_backend.comment;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.activity.ActivityAction;
import com.servicedesk.servicedesk_backend.activity.ActivityService;
import com.servicedesk.servicedesk_backend.activity.ActivityVisibility;

import com.servicedesk.servicedesk_backend.category.exception.ResourceNotFoundException;

import com.servicedesk.servicedesk_backend.comment.dto.AgentCommentRequest;
import com.servicedesk.servicedesk_backend.comment.dto.CommentResponse;
import com.servicedesk.servicedesk_backend.comment.dto.PublicCommentRequest;

import com.servicedesk.servicedesk_backend.ticket.Ticket;
import com.servicedesk.servicedesk_backend.ticket.TicketRepository;

import com.servicedesk.servicedesk_backend.user.Role;
import com.servicedesk.servicedesk_backend.user.User;
import com.servicedesk.servicedesk_backend.user.UserRepository;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


// STEP 2: DECLARE COMMENT SERVICE

@Service
@Transactional
public class CommentService {

    // STEP 3: DECLARE REQUIRED DEPENDENCIES

    private final CommentRepository commentRepository;

    private final TicketRepository ticketRepository;

    private final UserRepository userRepository;

    private final ActivityService activityService;


    // STEP 4: CONSTRUCTOR INJECTION

    public CommentService(
            CommentRepository commentRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository,
            ActivityService activityService
    ) {

        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.activityService = activityService;
    }


    // STEP 5: EMPLOYEE ADDS A PUBLIC COMMENT

    public CommentResponse addEmployeeComment(
            Long ticketId,
            String employeeEmail,
            PublicCommentRequest request
    ) {

        // STEP 5.1: FIND LOGGED-IN EMPLOYEE
        User employee = findEnabledUser(employeeEmail);


        // STEP 5.2: FIND REQUESTED TICKET
        Ticket ticket = findTicket(ticketId);


        // STEP 5.3: VERIFY TICKET OWNERSHIP

        // Employees can only comment on their own tickets.
        if (!ticket.getCreatedBy().getId().equals(employee.getId())) {

            throw new AccessDeniedException(
                    "You cannot comment on another employee's ticket"
            );
        }


        // STEP 5.4: CREATE COMMENT

        // IMPORTANT: Visibility is set by the backend.
        // An employee cannot create an internal note.
        Comment comment = new Comment(
                ticket,
                employee,
                request.content().trim(),
                CommentVisibility.PUBLIC
        );


        // STEP 5.5: SAVE COMMENT
        Comment savedComment = commentRepository.save(comment);


        // STEP 5.6: RECORD COMMENT ACTIVITY

        activityService.recordActivity(
                ticket,
                employee,
                ActivityAction.COMMENT_ADDED,
                "Public comment added",
                ActivityVisibility.PUBLIC
        );


        // STEP 5.7: CONVERT AND RETURN RESPONSE
        return convertToResponse(savedComment);
    }


    // STEP 6: AGENT OR ADMIN ADDS A COMMENT

    public CommentResponse addAgentComment(
            Long ticketId,
            String currentUserEmail,
            AgentCommentRequest request,
            boolean adminOverride
    ) {

        // STEP 6.1: FIND LOGGED-IN USER
        User currentUser = findEnabledUser(currentUserEmail);


        // STEP 6.2: FIND REQUESTED TICKET
        Ticket ticket = findTicket(ticketId);


        // STEP 6.3: VERIFY AGENT OR ADMIN ACCESS

        verifyAgentAccess(
                ticket,
                currentUser,
                adminOverride
        );


        // STEP 6.4: CREATE COMMENT

        Comment comment = new Comment(
                ticket,
                currentUser,
                request.content().trim(),
                request.visibility()
        );


        // STEP 6.5: SAVE COMMENT
        Comment savedComment = commentRepository.save(comment);


        // STEP 6.6: DETERMINE ACTIVITY TYPE AND VISIBILITY

        ActivityAction action;
        ActivityVisibility activityVisibility;

        if (request.visibility() == CommentVisibility.INTERNAL) {

            action = ActivityAction.INTERNAL_NOTE_ADDED;

            activityVisibility = ActivityVisibility.INTERNAL;

        } else {

            action = ActivityAction.COMMENT_ADDED;

            activityVisibility = ActivityVisibility.PUBLIC;
        }


        // STEP 6.7: RECORD ACTIVITY

        // Internal notes generate internal activity events.
        // Public comments generate public activity events.
        activityService.recordActivity(
                ticket,
                currentUser,
                action,
                request.visibility() == CommentVisibility.INTERNAL
                        ? "Internal note added"
                        : "Public comment added",
                activityVisibility
        );

// STEP 6.8: CHECK WHETHER THIS IS A PUBLIC AGENT RESPONSE

// Internal notes must not count as first responses.
// Employee comments must not count as agent responses.
        if (
                request.visibility() == CommentVisibility.PUBLIC
                        && currentUser.getRole() != Role.EMPLOYEE
        ) {

            // STEP 6.9: RECORD FIRST RESPONSE TIME

            // This method returns true only for the first response.
            boolean firstResponseRecorded = ticket.recordFirstResponse();


            // STEP 6.10: SAVE FIRST RESPONSE TIME AND ACTIVITY
            if (firstResponseRecorded) {

                // Save actual first-response timestamp in tickets table.
                ticketRepository.save(ticket);


                // Add first-response event to ticket activity history.
                activityService.recordActivity(
                        ticket,
                        currentUser,
                        ActivityAction.FIRST_RESPONSE_RECORDED,
                        "First response sent",
                        ActivityVisibility.PUBLIC
                );
            }
        }
        // STEP 6.8: RETURN COMMENT RESPONSE
        return convertToResponse(savedComment);
    }


    // STEP 7: FETCH EMPLOYEE PUBLIC COMMENTS

    @Transactional(readOnly = true)
    public List<CommentResponse> getEmployeeComments(
            Long ticketId,
            String employeeEmail
    ) {

        // STEP 7.1: FIND LOGGED-IN EMPLOYEE
        User employee = findEnabledUser(employeeEmail);


        // STEP 7.2: FIND TICKET
        Ticket ticket = findTicket(ticketId);


        // STEP 7.3: VERIFY TICKET OWNERSHIP
        if (!ticket.getCreatedBy().getId().equals(employee.getId())) {

            throw new AccessDeniedException(
                    "You cannot view comments on another employee's ticket"
            );
        }


        // STEP 7.4: FETCH ONLY PUBLIC COMMENTS

        List<Comment> comments = commentRepository
                .findByTicket_IdAndVisibilityOrderByCreatedAtAsc(
                        ticketId,
                        CommentVisibility.PUBLIC
                );


        // STEP 7.5: CONVERT COMMENT ENTITIES INTO DTOs
        return comments.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // STEP 8: FETCH ALL COMMENTS FOR AGENT OR ADMIN

    @Transactional(readOnly = true)
    public List<CommentResponse> getAgentComments(
            Long ticketId,
            String currentUserEmail,
            boolean adminOverride
    ) {

        // STEP 8.1: FIND LOGGED-IN USER
        User currentUser = findEnabledUser(currentUserEmail);


        // STEP 8.2: FIND TICKET
        Ticket ticket = findTicket(ticketId);


        // STEP 8.3: VERIFY ACCESS
        verifyAgentAccess(
                ticket,
                currentUser,
                adminOverride
        );


        // STEP 8.4: FETCH PUBLIC AND INTERNAL COMMENTS
        List<Comment> comments = commentRepository
                .findByTicket_IdOrderByCreatedAtAsc(ticketId);


        // STEP 8.5: CONVERT ENTITIES INTO DTOs
        return comments.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // STEP 9: VERIFY AGENT OR ADMIN ACCESS

    private void verifyAgentAccess(
            Ticket ticket,
            User currentUser,
            boolean adminOverride
    ) {

        // STEP 9.1: VERIFY ADMIN OVERRIDE

        if (adminOverride) {

            // Only a real ADMIN can use the override.
            if (currentUser.getRole() != Role.ADMIN) {

                throw new AccessDeniedException(
                        "Admin permission is required"
                );
            }

            return;
        }


        // STEP 9.2: VERIFY CURRENT USER IS AN AGENT

        if (currentUser.getRole() != Role.AGENT) {

            throw new AccessDeniedException(
                    "Only assigned agents or admins can access these comments"
            );
        }


        // STEP 9.3: VERIFY AGENT ASSIGNMENT

        boolean assignedToCurrentAgent =
                ticket.getAssignedTo() != null
                        && ticket.getAssignedTo()
                        .getId()
                        .equals(currentUser.getId());


        // STEP 9.4: REJECT UNASSIGNED OR OTHER AGENT ACCESS

        if (!assignedToCurrentAgent) {

            throw new AccessDeniedException(
                    "You can only access comments on tickets assigned to you"
            );
        }
    }


    // STEP 10: FIND ACTIVE USER BY EMAIL

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


    // STEP 12: CONVERT COMMENT ENTITY INTO RESPONSE DTO

    private CommentResponse convertToResponse(Comment comment) {

        // STEP 12.1: GET COMMENT AUTHOR
        User author = comment.getAuthor();


        // STEP 12.2: CREATE RESPONSE DTO
        return new CommentResponse(

                comment.getId(),

                comment.getTicket().getId(),

                comment.getContent(),

                comment.getVisibility(),

                author.getId(),

                author.getName(),

                author.getRole(),

                comment.getCreatedAt()
        );
    }

}

package com.servicedesk.servicedesk_backend.notification;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.category.exception.ResourceNotFoundException;

import com.servicedesk.servicedesk_backend.notification.dto.NotificationPageResponse;
import com.servicedesk.servicedesk_backend.notification.dto.NotificationResponse;

import com.servicedesk.servicedesk_backend.ticket.Ticket;
import com.servicedesk.servicedesk_backend.ticket.TicketStatus;

import com.servicedesk.servicedesk_backend.user.Role;
import com.servicedesk.servicedesk_backend.user.User;
import com.servicedesk.servicedesk_backend.user.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


// STEP 2: DECLARE NOTIFICATION SERVICE

@Service
@Transactional
public class NotificationService {

    // STEP 3: INJECT REQUIRED REPOSITORIES

    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;


    // STEP 4: CONSTRUCTOR INJECTION

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository
    ) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }


    // STEP 5: CREATE NOTIFICATION

    private void createNotification(
            User recipient,
            Ticket ticket,
            NotificationType type,
            String title,
            String message
    ) {

        // STEP 5.1: CHECK RECIPIENT

        if (recipient == null || !recipient.isEnabled()) {
            return;
        }


        // STEP 5.2: CREATE NOTIFICATION OBJECT

        Notification notification = new Notification(
                recipient,
                ticket,
                type,
                title,
                message
        );


        // STEP 5.3: SAVE NOTIFICATION

        notificationRepository.save(notification);
    }


    // STEP 6: NOTIFY USERS WHEN A TICKET IS ASSIGNED

    public void notifyTicketAssigned(
            Ticket ticket,
            User previousAgent
    ) {

        // STEP 6.1: GET CURRENT ASSIGNED AGENT

        User assignedAgent = ticket.getAssignedTo();


        // STEP 6.2: CHECK WHETHER ASSIGNMENT ACTUALLY CHANGED

        if (isSameUser(previousAgent, assignedAgent)) {
            return;
        }


        // STEP 6.3: GET TICKET DETAILS

        Long ticketId = ticket.getId();

        String ticketSubject = ticket.getSubject();

        boolean isReassignment = previousAgent != null;


        // STEP 6.4: NOTIFY NEW AGENT

        if (assignedAgent != null) {

            createNotification(
                    assignedAgent,
                    ticket,
                    isReassignment
                            ? NotificationType.TICKET_REASSIGNED
                            : NotificationType.TICKET_ASSIGNED,
                    isReassignment
                            ? "Ticket reassigned to you"
                            : "New ticket assigned to you",
                    "Ticket #" + ticketId
                            + " - " + ticketSubject
                            + " has been assigned to you."
            );
        }


        // STEP 6.5: NOTIFY EMPLOYEE

        User employee = ticket.getCreatedBy();

        createNotification(
                employee,
                ticket,
                isReassignment
                        ? NotificationType.TICKET_REASSIGNED
                        : NotificationType.TICKET_ASSIGNED,
                isReassignment
                        ? "Your ticket has been reassigned"
                        : "Your ticket has been assigned",
                isReassignment
                        ? "Your ticket #" + ticketId
                        + " has been reassigned to another agent."
                        : "An agent has been assigned to your ticket #"
                        + ticketId + "."
        );


        // STEP 6.6: NOTIFY PREVIOUS AGENT

        if (previousAgent != null) {

            createNotification(
                    previousAgent,
                    ticket,
                    NotificationType.TICKET_REASSIGNED,
                    "Ticket reassigned",
                    "Ticket #" + ticketId
                            + " - " + ticketSubject
                            + " has been reassigned to another agent."
            );
        }
    }


    // STEP 7: NOTIFY USERS WHEN TICKET STATUS CHANGES

    public void notifyTicketStatusChanged(
            Ticket ticket,
            User performedBy,
            TicketStatus previousStatus
    ) {

        // STEP 7.1: GET CURRENT STATUS

        TicketStatus currentStatus = ticket.getStatus();


        // STEP 7.2: PREPARE NOTIFICATION MESSAGE

        String message = "Ticket #"
                + ticket.getId()
                + " - "
                + ticket.getSubject()
                + " changed from "
                + previousStatus
                + " to "
                + currentStatus
                + ".";


        // STEP 7.3: NOTIFY EMPLOYEE

        User employee = ticket.getCreatedBy();

        if (!isSameUser(employee, performedBy)) {

            createNotification(
                    employee,
                    ticket,
                    NotificationType.TICKET_STATUS_CHANGED,
                    "Ticket status updated",
                    message
            );
        }


        // STEP 7.4: NOTIFY ASSIGNED AGENT

        User assignedAgent = ticket.getAssignedTo();

        if (!isSameUser(assignedAgent, performedBy)) {

            createNotification(
                    assignedAgent,
                    ticket,
                    NotificationType.TICKET_STATUS_CHANGED,
                    "Ticket status updated",
                    message
            );
        }
    }


    // STEP 8: NOTIFY USERS WHEN A PUBLIC COMMENT IS ADDED

    public void notifyCommentAdded(
            Ticket ticket,
            User author,
            boolean isPublic
    ) {

        // STEP 8.1: IGNORE INTERNAL NOTES

        if (!isPublic) {
            return;
        }


        // STEP 8.2: CHECK WHO ADDED THE COMMENT

        if (author.getRole() == Role.EMPLOYEE) {

            // STEP 8.3: GET ASSIGNED AGENT

            User assignedAgent = ticket.getAssignedTo();


            // STEP 8.4: NOTIFY ASSIGNED AGENT

            if (assignedAgent != null && assignedAgent.isEnabled()) {

                createNotification(
                        assignedAgent,
                        ticket,
                        NotificationType.COMMENT_ADDED,
                        "New comment from employee",
                        author.getName()
                                + " added a comment to ticket #"
                                + ticket.getId()
                                + "."
                );

            } else {

                // STEP 8.5: NOTIFY ACTIVE ADMINS
                // This handles tickets that do not have an assigned agent.

                List<User> admins =
                        userRepository.findByRoleAndEnabledTrue(Role.ADMIN);

                admins.stream()
                        .filter(admin -> !isSameUser(admin, author))
                        .forEach(admin -> createNotification(
                                admin,
                                ticket,
                                NotificationType.COMMENT_ADDED,
                                "New comment on unassigned ticket",
                                author.getName()
                                        + " added a comment to unassigned ticket #"
                                        + ticket.getId()
                                        + "."
                        ));
            }

        } else {

            // STEP 8.6: NOTIFY EMPLOYEE ABOUT AGENT OR ADMIN REPLY

            User employee = ticket.getCreatedBy();

            if (!isSameUser(employee, author)) {

                createNotification(
                        employee,
                        ticket,
                        NotificationType.COMMENT_ADDED,
                        "New reply on your ticket",
                        author.getName()
                                + " replied to your ticket #"
                                + ticket.getId()
                                + "."
                );
            }
        }
    }


    // STEP 9: FETCH LOGGED-IN USER'S NOTIFICATIONS

    @Transactional(readOnly = true)
    public NotificationPageResponse getMyNotifications(
            String email,
            int page,
            int size
    ) {

        // STEP 9.1: VALIDATE PAGINATION VALUES

        if (page < 0) {

            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 50) {

            throw new IllegalArgumentException(
                    "Page size must be between 1 and 50"
            );
        }


        // STEP 9.2: FIND LOGGED-IN USER

        User recipient = findEnabledUser(email);


        // STEP 9.3: CREATE PAGINATION REQUEST

        Pageable pageable = PageRequest.of(page, size);


        // STEP 9.4: FETCH ONLY THIS USER'S NOTIFICATIONS

        Page<Notification> notificationPage =
                notificationRepository
                        .findByRecipient_IdOrderByCreatedAtDesc(
                                recipient.getId(),
                                pageable
                        );


        // STEP 9.5: CONVERT NOTIFICATIONS INTO RESPONSE DTOs

        List<NotificationResponse> notifications =
                notificationPage.getContent()
                        .stream()
                        .map(this::convertToResponse)
                        .toList();


        // STEP 9.6: RETURN PAGINATED RESPONSE

        return new NotificationPageResponse(
                notifications,
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages(),
                notificationPage.isFirst(),
                notificationPage.isLast()
        );
    }


    // STEP 10: FETCH UNREAD NOTIFICATION COUNT

    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {

        // STEP 10.1: FIND LOGGED-IN USER

        User recipient = findEnabledUser(email);


        // STEP 10.2: COUNT UNREAD NOTIFICATIONS

        return notificationRepository
                .countByRecipient_IdAndReadAtIsNull(
                        recipient.getId()
                );
    }


    // STEP 11: MARK ONE NOTIFICATION AS READ

    public NotificationResponse markAsRead(
            Long notificationId,
            String email
    ) {

        // STEP 11.1: FIND LOGGED-IN USER

        User recipient = findEnabledUser(email);


        // STEP 11.2: FIND NOTIFICATION OWNED BY THIS USER

        Notification notification = notificationRepository
                .findByIdAndRecipient_Id(
                        notificationId,
                        recipient.getId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with ID: " + notificationId
                ));


        // STEP 11.3: MARK NOTIFICATION AS READ

        notification.markAsRead();


        // STEP 11.4: SAVE UPDATED NOTIFICATION

        Notification updatedNotification =
                notificationRepository.save(notification);


        // STEP 11.5: RETURN UPDATED NOTIFICATION

        return convertToResponse(updatedNotification);
    }


    // STEP 12: MARK ALL NOTIFICATIONS AS READ

    public int markAllAsRead(String email) {

        // STEP 12.1: FIND LOGGED-IN USER

        User recipient = findEnabledUser(email);


        // STEP 12.2: FETCH UNREAD NOTIFICATIONS

        List<Notification> unreadNotifications =
                notificationRepository
                        .findByRecipient_IdAndReadAtIsNullOrderByCreatedAtDesc(
                                recipient.getId()
                        );


        // STEP 12.3: MARK EVERY NOTIFICATION AS READ

        unreadNotifications.forEach(Notification::markAsRead);


        // STEP 12.4: SAVE UPDATED NOTIFICATIONS

        notificationRepository.saveAll(unreadNotifications);


        // STEP 12.5: RETURN NUMBER OF UPDATED NOTIFICATIONS

        return unreadNotifications.size();
    }


    // STEP 13: CONVERT ENTITY INTO RESPONSE DTO

    private NotificationResponse convertToResponse(
            Notification notification
    ) {

        // STEP 13.1: GET RELATED TICKET

        Ticket ticket = notification.getTicket();


        // STEP 13.2: CREATE RESPONSE DTO

        return new NotificationResponse(
                notification.getId(),
                ticket.getId(),
                ticket.getSubject(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }


    // STEP 14: FIND ENABLED USER BY EMAIL

    private User findEnabledUser(String email) {

        return userRepository
                .findByEmailIgnoreCase(email)
                .filter(User::isEnabled)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active user not found"
                ));
    }


    // STEP 15: CHECK WHETHER TWO REFERENCES BELONG TO THE SAME USER

    private boolean isSameUser(User first, User second) {

        return first != null
                && second != null
                && first.getId() != null
                && first.getId().equals(second.getId());
    }
}

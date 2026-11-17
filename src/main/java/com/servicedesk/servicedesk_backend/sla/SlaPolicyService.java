package com.servicedesk.servicedesk_backend.sla;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.category.exception.ResourceNotFoundException;

import com.servicedesk.servicedesk_backend.sla.dto.SlaPolicyRequest;
import com.servicedesk.servicedesk_backend.sla.dto.SlaPolicyResponse;

import com.servicedesk.servicedesk_backend.ticket.Ticket;
import com.servicedesk.servicedesk_backend.ticket.TicketPriority;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


// STEP 2: DECLARE SLA POLICY SERVICE

@Service
@Transactional
public class SlaPolicyService {

    // STEP 3: INJECT SLA POLICY REPOSITORY

    private final SlaPolicyRepository slaPolicyRepository;


    // STEP 4: CONSTRUCTOR INJECTION

    public SlaPolicyService(SlaPolicyRepository slaPolicyRepository) {

        this.slaPolicyRepository = slaPolicyRepository;
    }


    // STEP 5: FETCH ALL SLA POLICIES

    @Transactional(readOnly = true)
    public List<SlaPolicyResponse> getAllPolicies() {

        // STEP 5.1: FETCH POLICIES FROM DATABASE
        List<SlaPolicy> policies =
                slaPolicyRepository.findAllByOrderByPriorityAsc();


        // STEP 5.2: CONVERT ENTITIES INTO RESPONSE DTOs
        return policies.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // STEP 6: FETCH POLICY USING ID

    @Transactional(readOnly = true)
    public SlaPolicyResponse getPolicy(Long policyId) {

        // STEP 6.1: FIND POLICY
        SlaPolicy policy = findPolicyById(policyId);


        // STEP 6.2: RETURN RESPONSE DTO
        return convertToResponse(policy);
    }


    // STEP 7: CREATE NEW SLA POLICY

    public SlaPolicyResponse createPolicy(SlaPolicyRequest request) {

        // STEP 7.1: CHECK WHETHER PRIORITY ALREADY HAS A POLICY
        if (slaPolicyRepository.existsByPriority(request.priority())) {

            throw new IllegalArgumentException(
                    "An SLA policy already exists for priority: "
                            + request.priority()
            );
        }


        // STEP 7.2: CREATE POLICY OBJECT
        SlaPolicy policy = new SlaPolicy(
                request.priority(),
                request.firstResponseMinutes(),
                request.resolutionMinutes()
        );


        // STEP 7.3: SAVE POLICY
        SlaPolicy savedPolicy = slaPolicyRepository.save(policy);


        // STEP 7.4: RETURN RESPONSE
        return convertToResponse(savedPolicy);
    }


    // STEP 8: UPDATE EXISTING SLA POLICY

    public SlaPolicyResponse updatePolicy(
            Long policyId,
            SlaPolicyRequest request
    ) {

        // STEP 8.1: FIND POLICY
        SlaPolicy policy = findPolicyById(policyId);


        // STEP 8.2: CHECK DUPLICATE PRIORITY

        // Do not allow two policies with the same priority.
        boolean duplicatePriority =
                slaPolicyRepository.existsByPriorityAndIdNot(
                        request.priority(),
                        policyId
                );

        if (duplicatePriority) {

            throw new IllegalArgumentException(
                    "An SLA policy already exists for priority: "
                            + request.priority()
            );
        }


        // STEP 8.3: UPDATE POLICY DETAILS
        policy.updatePolicy(
                request.priority(),
                request.firstResponseMinutes(),
                request.resolutionMinutes()
        );


        // STEP 8.4: SAVE UPDATED POLICY
        SlaPolicy updatedPolicy = slaPolicyRepository.save(policy);


        // STEP 8.5: RETURN UPDATED POLICY
        return convertToResponse(updatedPolicy);
    }


    // STEP 9: CHANGE POLICY ACTIVE STATUS

    public SlaPolicyResponse updatePolicyStatus(
            Long policyId,
            boolean active
    ) {

        // STEP 9.1: FIND POLICY
        SlaPolicy policy = findPolicyById(policyId);


        // STEP 9.2: UPDATE ACTIVE STATUS
        policy.updateStatus(active);


        // STEP 9.3: SAVE CHANGES
        SlaPolicy updatedPolicy = slaPolicyRepository.save(policy);


        // STEP 9.4: RETURN UPDATED POLICY
        return convertToResponse(updatedPolicy);
    }


    // STEP 10: APPLY SLA POLICY TO NEW TICKET

    public void applyPolicyToTicket(Ticket ticket) {

        // STEP 10.1: FIND ACTIVE POLICY FOR TICKET PRIORITY
        SlaPolicy policy = slaPolicyRepository
                .findByPriorityAndActiveTrue(ticket.getPriority())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active SLA policy found for priority: "
                                + ticket.getPriority()
                ));


        // STEP 10.2: GET TICKET CREATION TIME
        LocalDateTime createdAt = ticket.getCreatedAt();

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }


        // STEP 10.3: CALCULATE FIRST RESPONSE DEADLINE
        LocalDateTime firstResponseDueAt = createdAt.plusMinutes(
                policy.getFirstResponseMinutes()
        );


        // STEP 10.4: CALCULATE RESOLUTION DEADLINE
        LocalDateTime resolutionDueAt = createdAt.plusMinutes(
                policy.getResolutionMinutes()
        );


        // STEP 10.5: STORE DEADLINES IN TICKET ENTITY
        ticket.setSlaDeadlines(
                firstResponseDueAt,
                resolutionDueAt
        );
    }


    // STEP 11: REUSABLE METHOD TO FIND POLICY

    private SlaPolicy findPolicyById(Long policyId) {

        return slaPolicyRepository
                .findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SLA policy not found with ID: " + policyId
                ));
    }


    // STEP 12: CONVERT POLICY ENTITY INTO RESPONSE DTO

    private SlaPolicyResponse convertToResponse(SlaPolicy policy) {

        return new SlaPolicyResponse(

                policy.getId(),

                policy.getPriority(),

                policy.getFirstResponseMinutes(),

                policy.getResolutionMinutes(),

                policy.isActive(),

                policy.getCreatedAt(),

                policy.getUpdatedAt()
        );
    }

}
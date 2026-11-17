package com.servicedesk.servicedesk_backend.sla.controller;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.sla.SlaPolicyService;

import com.servicedesk.servicedesk_backend.sla.dto.SlaPolicyRequest;
import com.servicedesk.servicedesk_backend.sla.dto.SlaPolicyResponse;
import com.servicedesk.servicedesk_backend.sla.dto.SlaPolicyStatusRequest;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;


// STEP 2: DECLARE ADMIN SLA CONTROLLER

@RestController
@RequestMapping("/api/admin/sla-policies")
public class AdminSlaController {

    // STEP 3: INJECT SLA POLICY SERVICE
    private final SlaPolicyService slaPolicyService;


    // STEP 4: CONSTRUCTOR INJECTION
    public AdminSlaController(SlaPolicyService slaPolicyService) {

        this.slaPolicyService = slaPolicyService;
    }


    // STEP 5: FETCH ALL SLA POLICIES

    @GetMapping
    public ResponseEntity<List<SlaPolicyResponse>> getAllPolicies() {

        // STEP 5.1: FETCH POLICIES
        List<SlaPolicyResponse> policies =
                slaPolicyService.getAllPolicies();


        // STEP 5.2: RETURN POLICY LIST
        return ResponseEntity.ok(policies);
    }


    // STEP 6: FETCH POLICY BY ID

    @GetMapping("/{policyId}")
    public ResponseEntity<SlaPolicyResponse> getPolicy(

            @PathVariable Long policyId
    ) {

        // STEP 6.1: FETCH POLICY DETAILS
        SlaPolicyResponse policy =
                slaPolicyService.getPolicy(policyId);


        // STEP 6.2: RETURN POLICY
        return ResponseEntity.ok(policy);
    }


    // STEP 7: CREATE SLA POLICY

    @PostMapping
    public ResponseEntity<SlaPolicyResponse> createPolicy(

            @Valid @RequestBody SlaPolicyRequest request
    ) {

        // STEP 7.1: CREATE POLICY
        SlaPolicyResponse policy =
                slaPolicyService.createPolicy(request);


        // STEP 7.2: RETURN HTTP 201 CREATED
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(policy);
    }


    // STEP 8: UPDATE SLA POLICY

    @PutMapping("/{policyId}")
    public ResponseEntity<SlaPolicyResponse> updatePolicy(

            @PathVariable Long policyId,

            @Valid @RequestBody SlaPolicyRequest request
    ) {

        // STEP 8.1: UPDATE POLICY
        SlaPolicyResponse policy =
                slaPolicyService.updatePolicy(
                        policyId,
                        request
                );


        // STEP 8.2: RETURN UPDATED POLICY
        return ResponseEntity.ok(policy);
    }


    // STEP 9: CHANGE SLA POLICY STATUS

    @PatchMapping("/{policyId}/status")
    public ResponseEntity<SlaPolicyResponse> updatePolicyStatus(

            @PathVariable Long policyId,

            @Valid @RequestBody SlaPolicyStatusRequest request
    ) {

        // STEP 9.1: UPDATE ACTIVE STATUS
        SlaPolicyResponse policy =
                slaPolicyService.updatePolicyStatus(
                        policyId,
                        request.active()
                );


        // STEP 9.2: RETURN UPDATED POLICY
        return ResponseEntity.ok(policy);
    }

}
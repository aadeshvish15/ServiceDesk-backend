package com.servicedesk.servicedesk_backend.sla;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.ticket.TicketPriority;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


// STEP 2: DECLARE SLA POLICY DATA INITIALIZER

@Component
public class SlaPolicyDataInitializer implements ApplicationRunner {

    // STEP 3: INJECT SLA POLICY REPOSITORY
    private final SlaPolicyRepository slaPolicyRepository;


    // STEP 4: CONSTRUCTOR INJECTION
    public SlaPolicyDataInitializer(
            SlaPolicyRepository slaPolicyRepository
    ) {

        this.slaPolicyRepository = slaPolicyRepository;
    }


    // STEP 5: RUN INITIALIZATION AFTER APPLICATION STARTUP

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // STEP 5.1: CREATE LOW PRIORITY POLICY IF MISSING
        createPolicyIfMissing(
                TicketPriority.LOW,
                480,
                2880
        );


        // STEP 5.2: CREATE MEDIUM PRIORITY POLICY IF MISSING
        createPolicyIfMissing(
                TicketPriority.MEDIUM,
                240,
                1440
        );


        // STEP 5.3: CREATE HIGH PRIORITY POLICY IF MISSING
        createPolicyIfMissing(
                TicketPriority.HIGH,
                60,
                480
        );


        // STEP 5.4: CREATE CRITICAL PRIORITY POLICY IF MISSING
        createPolicyIfMissing(
                TicketPriority.CRITICAL,
                15,
                120
        );
    }


    // STEP 6: REUSABLE METHOD TO CREATE MISSING POLICY

    private void createPolicyIfMissing(
            TicketPriority priority,
            int firstResponseMinutes,
            int resolutionMinutes
    ) {

        // STEP 6.1: CHECK WHETHER POLICY EXISTS
        if (slaPolicyRepository.existsByPriority(priority)) {
            return;
        }


        // STEP 6.2: CREATE POLICY
        SlaPolicy policy = new SlaPolicy(
                priority,
                firstResponseMinutes,
                resolutionMinutes
        );


        // STEP 6.3: SAVE POLICY
        slaPolicyRepository.save(policy);
    }

}
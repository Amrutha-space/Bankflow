package com.bankflow.onboarding.controller;

import com.bankflow.onboarding.dto.StartOnboardingRequest;
import com.bankflow.onboarding.entity.OnboardingApplication;
import com.bankflow.onboarding.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(
            OnboardingService onboardingService) {

        this.onboardingService = onboardingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OnboardingApplication start(
            @Valid @RequestBody StartOnboardingRequest request) {

        return onboardingService.start(request);
    }

    @GetMapping("/{id}")
    public OnboardingApplication get(
            @PathVariable UUID id) {

        return onboardingService.get(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<OnboardingApplication> getCustomerApplications(
            @PathVariable UUID customerId) {

        return onboardingService
                .getCustomerApplications(customerId);
    }
}
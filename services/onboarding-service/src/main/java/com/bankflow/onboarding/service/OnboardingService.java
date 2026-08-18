package com.bankflow.onboarding.service;

import com.bankflow.onboarding.dto.StartOnboardingRequest;
import com.bankflow.onboarding.entity.OnboardingApplication;
import com.bankflow.onboarding.repository.OnboardingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OnboardingService {

    private final OnboardingRepository onboardingRepository;

    public OnboardingService(
            OnboardingRepository onboardingRepository) {

        this.onboardingRepository = onboardingRepository;
    }

    @Transactional
    public OnboardingApplication start(
            StartOnboardingRequest request) {

        OnboardingApplication application =
                new OnboardingApplication();

        application.setCustomerId(
                request.getCustomerId()
        );

        application.setAccountType(
                request.getAccountType()
        );

        return onboardingRepository.save(application);
    }

    public OnboardingApplication get(UUID id) {

        return onboardingRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Onboarding application not found"));
    }

    public List<OnboardingApplication>
    getCustomerApplications(UUID customerId) {

        return onboardingRepository
                .findByCustomerId(customerId);
    }
}
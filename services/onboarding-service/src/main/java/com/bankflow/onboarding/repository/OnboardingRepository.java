package com.bankflow.onboarding.repository;

import com.bankflow.onboarding.entity.OnboardingApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OnboardingRepository
        extends JpaRepository<OnboardingApplication, UUID> {

    List<OnboardingApplication> findByCustomerId(UUID customerId);
}
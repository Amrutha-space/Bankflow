package com.Bankflow.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Bankflow.customer.entity.Customer;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
}
package com.bankflow.corebanking.controller;

import com.bankflow.corebanking.dto.CreateAccountRequest;
import com.bankflow.corebanking.entity.Account;
import com.bankflow.corebanking.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(
            @Valid @RequestBody CreateAccountRequest request) {

        return accountService.createAccount(request);
    }

    @GetMapping("/{id}")
    public Account getAccount(@PathVariable UUID id) {

        return accountService.getAccount(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<Account> getCustomerAccounts(
            @PathVariable UUID customerId) {

        return accountService.getCustomerAccounts(customerId);
    }
}
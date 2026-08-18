package com.bankflow.corebanking.controller;

import com.bankflow.corebanking.dto.TransactionRequest;
import com.bankflow.corebanking.entity.BankTransaction;
import com.bankflow.corebanking.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(
            TransactionService transactionService) {

        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public BankTransaction deposit(
            @Valid @RequestBody TransactionRequest request) {

        return transactionService.deposit(request);
    }

    @PostMapping("/withdraw")
    @ResponseStatus(HttpStatus.CREATED)
    public BankTransaction withdraw(
            @Valid @RequestBody TransactionRequest request) {

        return transactionService.withdraw(request);
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public BankTransaction transfer(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransactionRequest request) {

        return transactionService.transfer(
                request,
                idempotencyKey
        );
    }

    @GetMapping("/account/{accountId}")
    public List<BankTransaction> getTransactions(
            @PathVariable UUID accountId) {

        return transactionService.getTransactions(accountId);
    }
}
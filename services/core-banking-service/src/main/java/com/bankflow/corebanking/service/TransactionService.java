package com.bankflow.corebanking.service;

import com.bankflow.corebanking.dto.TransactionRequest;
import com.bankflow.corebanking.entity.Account;
import com.bankflow.corebanking.entity.BankTransaction;
import com.bankflow.corebanking.entity.IdempotencyRecord;
import com.bankflow.corebanking.repository.AccountRepository;
import com.bankflow.corebanking.repository.BankTransactionRepository;
import com.bankflow.corebanking.repository.IdempotencyRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;

    public TransactionService(
            AccountRepository accountRepository,
            BankTransactionRepository transactionRepository,
            IdempotencyRecordRepository idempotencyRecordRepository) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
    }

    // =========================
    // DEPOSIT
    // =========================

    @Transactional
    public BankTransaction deposit(TransactionRequest request) {

        Account account = accountRepository.findByIdForUpdate(request.getAccountId())
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        validateAccount(account);

        account.setBalance(
                account.getBalance().add(amount)
        );

        accountRepository.save(account);

        BankTransaction transaction = new BankTransaction();

        transaction.setAccountId(account.getId());
        transaction.setType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setStatus("COMPLETED");
        transaction.setReference(UUID.randomUUID().toString());

        return transactionRepository.save(transaction);
    }

    // =========================
    // WITHDRAW
    // =========================

    @Transactional
    public BankTransaction withdraw(TransactionRequest request) {

        Account account = accountRepository.findByIdForUpdate(request.getAccountId())
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        validateAccount(account);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        account.setBalance(
                account.getBalance().subtract(amount)
        );

        accountRepository.save(account);

        BankTransaction transaction = new BankTransaction();

        transaction.setAccountId(account.getId());
        transaction.setType("WITHDRAW");
        transaction.setAmount(amount);
        transaction.setStatus("COMPLETED");
        transaction.setReference(UUID.randomUUID().toString());

        return transactionRepository.save(transaction);
    }

    // =========================
    // TRANSFER
    // =========================

    @Transactional
    public BankTransaction transfer(
            TransactionRequest request,
            String idempotencyKey) {

        // 1. Validate idempotency key
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new RuntimeException(
                    "Idempotency-Key header is required");
        }

        // 2. Check if this request was already processed
        var existingRecord =
                idempotencyRecordRepository
                        .findByIdempotencyKey(idempotencyKey);

        if (existingRecord.isPresent()) {

            UUID transactionId =
                    existingRecord.get().getTransactionId();

            return transactionRepository.findById(transactionId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Original transaction not found"));
        }

        UUID sourceId = request.getAccountId();
        UUID destinationId = request.getDestinationAccountId();

        if (destinationId == null) {
            throw new RuntimeException(
                    "Destination account is required");
        }

        if (sourceId.equals(destinationId)) {
            throw new RuntimeException(
                    "Source and destination accounts must be different");
        }

        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "Amount must be greater than zero");
        }

        /*
         * Always lock accounts in the same order.
         *
         * This helps prevent deadlocks when two transfers
         * happen at the same time.
         */
        UUID firstId;
        UUID secondId;

        if (sourceId.compareTo(destinationId) < 0) {
            firstId = sourceId;
            secondId = destinationId;
        } else {
            firstId = destinationId;
            secondId = sourceId;
        }

        // 3. Lock first account
        Account firstAccount = accountRepository
                .findByIdForUpdate(firstId)
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        // 4. Lock second account
        Account secondAccount = accountRepository
                .findByIdForUpdate(secondId)
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        Account sourceAccount;

        if (firstAccount.getId().equals(sourceId)) {
            sourceAccount = firstAccount;
        } else {
            sourceAccount = secondAccount;
        }

        Account destinationAccount;

        if (firstAccount.getId().equals(destinationId)) {
            destinationAccount = firstAccount;
        } else {
            destinationAccount = secondAccount;
        }

        validateAccount(sourceAccount);
        validateAccount(destinationAccount);

        // 5. Check sufficient balance
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException(
                    "Insufficient balance");
        }

        // 6. Debit source account
        sourceAccount.setBalance(
                sourceAccount.getBalance().subtract(amount)
        );

        // 7. Credit destination account
        destinationAccount.setBalance(
                destinationAccount.getBalance().add(amount)
        );

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        // 8. Create transaction record
        BankTransaction transaction = new BankTransaction();

        transaction.setAccountId(sourceAccount.getId());

        transaction.setDestinationAccountId(
                destinationAccount.getId()
        );

        transaction.setType("TRANSFER");
        transaction.setAmount(amount);
        transaction.setStatus("COMPLETED");
        transaction.setReference(UUID.randomUUID().toString());

        BankTransaction savedTransaction =
                transactionRepository.save(transaction);

        // 9. Save idempotency record
        IdempotencyRecord record = new IdempotencyRecord();

        record.setIdempotencyKey(idempotencyKey);
        record.setTransactionId(savedTransaction.getId());

        idempotencyRecordRepository.save(record);

        // 10. Return transaction
        return savedTransaction;
    }

    // =========================
    // TRANSACTION HISTORY
    // =========================

    public List<BankTransaction> getTransactions(UUID accountId) {

        return transactionRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    // =========================
    // ACCOUNT VALIDATION
    // =========================

    private void validateAccount(Account account) {

        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException(
                    "Account is not active");
        }

        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }
    }
}
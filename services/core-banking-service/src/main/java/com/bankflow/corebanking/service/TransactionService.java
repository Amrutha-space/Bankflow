package com.bankflow.corebanking.service;

import com.bankflow.corebanking.dto.TransactionRequest;
import com.bankflow.corebanking.entity.Account;
import com.bankflow.corebanking.entity.BankTransaction;
import com.bankflow.corebanking.repository.AccountRepository;
import com.bankflow.corebanking.repository.BankTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;

    public TransactionService(
            AccountRepository accountRepository,
            BankTransactionRepository transactionRepository) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public BankTransaction deposit(TransactionRequest request) {

        Account account = accountRepository.findByIdForUpdate(request.getAccountId())
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        account.setBalance(account.getBalance().add(amount));

        accountRepository.save(account);

        BankTransaction transaction = new BankTransaction();

        transaction.setAccountId(account.getId());
        transaction.setType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setReference(UUID.randomUUID().toString());

        return transactionRepository.save(transaction);
    }
    @Transactional
public BankTransaction withdraw(TransactionRequest request) {

    Account account = accountRepository.findByIdForUpdate(request.getAccountId())
            .orElseThrow(() ->
                    new RuntimeException("Account not found"));

    BigDecimal amount = request.getAmount();

    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new RuntimeException("Amount must be greater than zero");
    }

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
    transaction.setReference(UUID.randomUUID().toString());

    return transactionRepository.save(transaction);
}

}

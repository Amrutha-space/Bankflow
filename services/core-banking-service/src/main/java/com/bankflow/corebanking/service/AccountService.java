package com.bankflow.corebanking.service;

import com.bankflow.corebanking.dto.CreateAccountRequest;
import com.bankflow.corebanking.entity.Account;
import com.bankflow.corebanking.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account createAccount(CreateAccountRequest request) {

        Account account = new Account();

        account.setCustomerId(request.getCustomerId());
        account.setAccountType(request.getAccountType());
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);

        return accountRepository.save(account);
    }

    public Account getAccount(UUID id) {

        return accountRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));
    }

    public List<Account> getCustomerAccounts(UUID customerId) {

        return accountRepository.findByCustomerId(customerId);
    }

    private String generateAccountNumber() {

        long number = ThreadLocalRandom.current()
                .nextLong(1000000000L, 9999999999L);

        return String.valueOf(number);
    }
}
package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.AccountRequest;
import com.turkcell.soccer.dto.AccountResponse;
import com.turkcell.soccer.model.Account;
import com.turkcell.soccer.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        // Check if username already exists
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        // Check if email already exists
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Create new account
        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setEmail(request.getEmail());
        account.setPassword(request.getPassword()); // In production, hash this password!

        // Save to database
        Account savedAccount = accountRepository.save(account);

        // Convert to response DTO
        return new AccountResponse(
            savedAccount.getId(),
            savedAccount.getUsername(),
            savedAccount.getEmail(),
            savedAccount.getCreatedAt()
        );
    }
}

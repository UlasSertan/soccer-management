package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.request.AccountRequest;
import com.turkcell.soccer.dto.response.AccountResponse;
import com.turkcell.soccer.model.Account;
import com.turkcell.soccer.model.Role;
import com.turkcell.soccer.repository.AccountRepository;
import com.turkcell.soccer.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountService(AccountRepository accountRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
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
        account.setPassword(passwordEncoder.encode(request.getPassword())); // In production, hash this password!


        Role role = roleRepository.findByName(Role.RoleName.USER.name()).orElseThrow(
                () -> new IllegalStateException("Default role USER not found"));

        account.setRole(role);
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


    @Transactional
    public void deleteAccount(String accountName) {
        Account account = accountRepository.findByUsername(accountName).orElseThrow(
                () -> new UsernameNotFoundException("Username not found: " + accountName)
        );

        accountRepository.delete(account);
    }

    public Account authenticate (String username, String password) {
        // Finding user
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        // Matching hashed passwords
        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return account;
    }
}

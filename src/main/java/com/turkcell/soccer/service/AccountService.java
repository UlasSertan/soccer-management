package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.request.AccountRequest;
import com.turkcell.soccer.dto.request.AccountUpdateRequest;
import com.turkcell.soccer.dto.response.AccountInfoResponse;
import com.turkcell.soccer.dto.response.AccountResponse;
import com.turkcell.soccer.dto.response.AccountUpdateResponse;
import com.turkcell.soccer.mapper.AccountMapper;
import com.turkcell.soccer.model.Account;
import com.turkcell.soccer.model.Role;
import com.turkcell.soccer.repository.AccountRepository;
import com.turkcell.soccer.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;

    @Autowired
    public AccountService(AccountRepository accountRepository, RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountMapper = accountMapper;
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
        return accountMapper.toAccountResponse(savedAccount);
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

    public AccountInfoResponse getAccountInfo(String accountName) {
        Account account = accountRepository.findByUsername(accountName).orElseThrow(
                () -> new UsernameNotFoundException("Username not found: " + accountName)
        );

        return accountMapper.toAccountInfoResponse(account);
    }

    public AccountUpdateResponse updateAccount(AccountUpdateRequest updateRequest, String username) {
        Account account = accountRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("Username not found: " + username)
        );

        if (updateRequest.getPassword() != null) {
            account.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }
        if (updateRequest.getEmail() != null) {
            account.setEmail(updateRequest.getEmail());
        }

        Account saved = accountRepository.save(account);


        return accountMapper.toAccountUpdateResponse(saved);

    }

    public Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException(
                    "No authentication found in security context");
        }

        return authentication;
    }

    public Account getAccount() {

        Authentication authentication = getAuthentication();

        String username = authentication.getName();


        return accountRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("Username not found")
        );
    }
}

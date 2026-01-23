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
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
            log.warn("Account creation failed: Username {} already exists", request.getUsername());
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        // Check if email already exists
        if (accountRepository.existsByEmail(request.getEmail())) {
            log.warn("Account creation failed: Email {} already exists", request.getEmail());
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Create new account
        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword())); // In production, hash this password!

        log.debug("Preparing account with username {} and email {}", request.getUsername(), request.getEmail());

        Role role = roleRepository.findByName(Role.RoleName.USER.name()).orElse(null);
        if (role == null) {
            log.warn("Account creation failed: Role {} not found", Role.RoleName.USER.name());
            throw new  IllegalStateException("Default role USER not found");
        }

        account.setRole(role);
        log.debug("Assigned role {} to account {}", role.getName(), account.getUsername());
        // Save to database
        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully. ID: {}, Username: {}", savedAccount.getId(), savedAccount.getUsername());        // Convert to response DTO
        return accountMapper.toAccountResponse(savedAccount);
    }


    @Transactional
    public void deleteAccount(String accountName) {
        Account account = getAccountFromRepo(accountName);

        accountRepository.delete(account);
        log.info("Account deleted successfully. ID: {}, Username: {}", account.getId(), account.getUsername());
    }

    public Account authenticate (String username, String password) {
        // Finding user
        Account account = getAccountFromRepo(username);

        // Matching hashed passwords
        if (!passwordEncoder.matches(password, account.getPassword())) {
            log.warn("Invalid credentials: Username {}", username);
            throw new BadCredentialsException("Invalid credentials");
        }

        return account;
    }

    public AccountInfoResponse getAccountInfo(String accountName) {
        Account account = getAccountFromRepo(accountName);

        return accountMapper.toAccountInfoResponse(account);
    }

    public AccountUpdateResponse updateAccount(AccountUpdateRequest updateRequest, String username) {
        Account account = getAccountFromRepo(username);

        if (updateRequest.getPassword() != null) {
            account.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
            log.debug("Updating password: Username {}", username);
        }
        if (updateRequest.getEmail() != null) {
            account.setEmail(updateRequest.getEmail());
            log.debug("Updating email: Username {}", username);
        }

        Account saved = accountRepository.save(account);
        log.info("Account updated successfully. ID: {}, Username: {}", saved.getId(), saved.getUsername());

        return accountMapper.toAccountUpdateResponse(saved);

    }

    public Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Authentication object is null or not authenticated");
            throw new AuthenticationCredentialsNotFoundException(
                    "No authentication found in security context");
        }

        return authentication;
    }

    public Account getAccount() {

        Authentication authentication = getAuthentication();

        String username = authentication.getName();


        return getAccountFromRepo(username);
    }

    private Account getAccountFromRepo(String username) {
        Account account = accountRepository.findByUsername(username).orElse(null);
        if (account == null) {
            log.warn("Username {} not found", username);
            throw new UsernameNotFoundException("Username not found: " + username);
        }

        return account;
    }
}

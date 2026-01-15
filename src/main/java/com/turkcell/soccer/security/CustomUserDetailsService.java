package com.turkcell.soccer.security;

import com.turkcell.soccer.model.Account;
import com.turkcell.soccer.repository.AccountRepository;
import lombok.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    public CustomUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    // Add non null for interface compatibility
    public @NonNull UserDetails loadUserByUsername(@NonNull String username)
        throws UsernameNotFoundException {

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .authorities("USER")
                .build();
    }


}

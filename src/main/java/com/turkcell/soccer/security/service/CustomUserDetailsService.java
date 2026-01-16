package com.turkcell.soccer.security.service;

import com.turkcell.soccer.model.Account;
import com.turkcell.soccer.model.Role;
import com.turkcell.soccer.repository.AccountRepository;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Give the role
        authorities.add(
                new SimpleGrantedAuthority(
                        Role.RoleName.valueOf(account.getRole().getName()).authority()
                )
        );

        // Give the permissions
        account.getRole().getPermissions().forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.getName()))
        );



        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .authorities(authorities) // All authorities
                .build();
    }




}

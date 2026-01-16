package com.turkcell.soccer.security.common;

import com.turkcell.soccer.model.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component ("accountSecurity")
public class AccountSecurity {

    public boolean canDeleteAccount(String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();


        System.out.println(">>> canDeleteAccount CALLED");
        System.out.println(">>> auth = " + auth);
        System.out.println(">>> name = " + (auth != null ? auth.getName() : "null"));
        System.out.println(">>> authenticated = " + (auth != null && auth.isAuthenticated()));
        System.out.println(">>> authorities = " + (auth != null ? auth.getAuthorities() : "null"));
        System.out.println(">>> target = " + username);


        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        if (auth.getAuthorities().stream().
                anyMatch(a -> a.getAuthority()
                        .equals(Role.RoleName.ADMIN.authority()))) {
            return true;
        }

        return auth.getName().equals(username);
    }
}

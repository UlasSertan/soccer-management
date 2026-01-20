package com.turkcell.soccer.security.common;

import com.turkcell.soccer.model.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component ("accountSecurity")
public class AccountSecurity {

    public boolean selfRequest(String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

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

    public boolean canDeleteAccount(String username) {
        return selfRequest(username);
    }

    public boolean canUpdateAccount(String username) {
        return selfRequest(username);
    }

    public boolean canSeeAccount(String username) {
        return selfRequest(username);
    }
}

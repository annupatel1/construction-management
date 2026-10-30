package com.construction.construction_management.security;

import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.entity.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Gives services access to the logged-in user, so ids are never trusted from the URL. */
@Component
public class CurrentUser {

    public User get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails details)) {
            throw new AccessDeniedException("Not authenticated");
        }
        return details.getUser();
    }

    public int id() {
        return get().getId();
    }

    public RoleName role() {
        return get().getRole().getName();
    }

    public boolean isAdmin() {
        return role() == RoleName.ADMIN;
    }
}

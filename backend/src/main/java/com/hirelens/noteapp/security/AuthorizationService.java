package com.hirelens.noteapp.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;


@Component("authz")
public class AuthorizationService {

    public boolean isSelfOrAdmin(Long targetUserId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || targetUserId == null) {
            return false;
        }
        return isAdmin(auth) || String.valueOf(targetUserId).equals(auth.getName());
    }

    public boolean isAdmin(Authentication auth) {
        if (auth == null) {
            return false;
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}

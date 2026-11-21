package com.iskcondhanbad.donordash.security;

import com.iskcondhanbad.donordash.utils.Constants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public final class SecurityUtils {

    private SecurityUtils() {
        // utility class
    }

    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(auth) || !auth.isAuthenticated() || Constants.ANONYMOUS_USER.equals(auth.getPrincipal())) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> {
            String authority = a.getAuthority();
            return "ROLE_ADMIN".equalsIgnoreCase(authority) || Constants.ADMIN.equalsIgnoreCase(authority);
        });
    }

    public static boolean isDonorCultivator() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(auth) || !auth.isAuthenticated() || Constants.ANONYMOUS_USER.equals(auth.getPrincipal())) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> {
            String authority = a.getAuthority();
            return "ROLE_DONORCULTIVATOR".equalsIgnoreCase(authority) || Constants.DONOR_CULTIVATOR.equalsIgnoreCase(authority);
        });
    }
}


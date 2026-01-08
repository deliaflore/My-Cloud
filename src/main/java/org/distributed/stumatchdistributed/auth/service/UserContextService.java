package org.distributed.stumatchdistributed.auth.service;

import org.distributed.stumatchdistributed.auth.entity.UserAccount;
import org.distributed.stumatchdistributed.auth.repository.UserAccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserContextService {

    private final UserAccountRepository userAccountRepository;

    public UserContextService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Gets the current authenticated user from SecurityContext.
     */
    public UserAccount getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        String email = auth.getName();
        return requireUserByEmail(email);
    }

    /**
     * Gets user by email (for backward compatibility or explicit lookups).
     */
    public UserAccount requireUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Missing user identity");
        }
        return userAccountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Unknown user: " + email));
    }
}


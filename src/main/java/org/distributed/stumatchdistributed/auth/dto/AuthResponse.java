package org.distributed.stumatchdistributed.auth.dto;

public record AuthResponse(
        boolean success,
        String message,
        String token
) {
    public AuthResponse(boolean success, String message) {
        this(success, message, null);
    }
}


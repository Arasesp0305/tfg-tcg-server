package com.tfg.tcgserver.config;

public record AuthenticatedUser(
        String uid,
        String email,
        boolean emailVerified
) {
}

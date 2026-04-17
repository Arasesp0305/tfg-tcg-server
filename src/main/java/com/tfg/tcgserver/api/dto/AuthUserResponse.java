package com.tfg.tcgserver.api.dto;

public record AuthUserResponse(
        String uid,
        String email,
        boolean emailVerified
) {
}

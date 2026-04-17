package com.tfg.tcgserver.controllers;

import com.tfg.tcgserver.config.AuthenticatedUser;
import com.tfg.tcgserver.config.FirebaseAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;

public abstract class AuthenticatedController {

    protected AuthenticatedUser authenticatedUser(HttpServletRequest request) {
        Object authenticatedUser = request.getAttribute(FirebaseAuthenticationFilter.AUTHENTICATED_USER_ATTRIBUTE);

        if (authenticatedUser instanceof AuthenticatedUser user) {
            return user;
        }

        throw new IllegalStateException("Usuario no autenticado");
    }

    protected void requireSameUser(String requestedUid, HttpServletRequest request) {
        AuthenticatedUser user = authenticatedUser(request);

        if (!user.uid().equals(requestedUid)) {
            throw new SecurityException("No puedes acceder a datos de otro usuario");
        }
    }
}

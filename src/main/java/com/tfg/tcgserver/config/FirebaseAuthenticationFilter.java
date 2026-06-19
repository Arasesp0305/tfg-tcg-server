package com.tfg.tcgserver.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";

    private static final Set<String> PUBLIC_API_PATHS = Set.of(
            "/api/auth/verify",
            "/api/firebase/health"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !path.startsWith("/api/")
                || PUBLIC_API_PATHS.contains(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "Falta el token de Firebase en Authorization: Bearer <token>");
            return;
        }

        String idToken = authorizationHeader.substring("Bearer ".length());

        try {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    token.getUid(),
                    token.getEmail(),
                    token.isEmailVerified()
            );

            request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, authenticatedUser);
            filterChain.doFilter(request, response);
        } catch (FirebaseAuthException exception) {
            sendUnauthorized(response, "Token de Firebase no valido o caducado");
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}

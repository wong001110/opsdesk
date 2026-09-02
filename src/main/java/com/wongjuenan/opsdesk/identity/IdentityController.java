package com.wongjuenan.opsdesk.identity;

import java.util.UUID;

import com.wongjuenan.opsdesk.common.ApiException;
import com.wongjuenan.opsdesk.security.OpsDeskPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class IdentityController {

    private final IdentityDirectory identities;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContexts;

    public IdentityController(
            IdentityDirectory identities,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContexts) {
        this.identities = identities;
        this.authenticationManager = authenticationManager;
        this.securityContexts = securityContexts;
    }

    @GetMapping("/csrf")
    CsrfView csrf(CsrfToken token) {
        return new CsrfView(token.getHeaderName(), token.getParameterName(), token.getToken());
    }

    @PostMapping("/login")
    CurrentUserView login(
            @Valid @RequestBody LoginRequest requestBody,
            HttpServletRequest request,
            HttpServletResponse response) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            requestBody.email(), requestBody.password()));
        } catch (AuthenticationException exception) {
            throw ApiException.unauthorized("Invalid email or password");
        }

        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            request.changeSessionId();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContexts.saveContext(context, request, response);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");

        return currentUser((OpsDeskPrincipal) authentication.getPrincipal());
    }

    @GetMapping("/me")
    CurrentUserView currentUser(@AuthenticationPrincipal OpsDeskPrincipal principal) {
        IdentityDirectory.UserIdentity identity = identities.requireById(principal.userId());
        return new CurrentUserView(
                identity.id(), identity.email(), identity.displayName(), identity.enabled());
    }

    record CurrentUserView(UUID id, String email, String displayName, boolean enabled) {
    }

    record CsrfView(String headerName, String parameterName, String token) {
    }

    record LoginRequest(
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(max = 1024) String password) {
    }
}

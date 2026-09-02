package com.wongjuenan.opsdesk.security;

import java.io.IOException;
import java.time.Instant;

import com.wongjuenan.opsdesk.common.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityContextRepository sessionSecurityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    AuthenticationEntryPoint apiAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, exception) ->
                writeError(objectMapper, request, response, HttpStatus.UNAUTHORIZED, "Authentication required");
    }

    @Bean
    AuthenticationEntryPoint basicAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, exception) -> {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"OpsDesk\"");
            writeError(objectMapper, request, response, HttpStatus.UNAUTHORIZED, "Authentication required");
        };
    }

    @Bean
    AccessDeniedHandler apiAccessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, exception) ->
                writeError(objectMapper, request, response, HttpStatus.FORBIDDEN, "Access denied");
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Qualifier("apiAuthenticationEntryPoint") AuthenticationEntryPoint apiAuthenticationEntryPoint,
            @Qualifier("basicAuthenticationEntryPoint") AuthenticationEntryPoint basicAuthenticationEntryPoint,
            AccessDeniedHandler apiAccessDeniedHandler) throws Exception {
        CookieCsrfTokenRepository csrfTokens = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokens.setCookieCustomizer(cookie -> cookie.path("/").sameSite("Lax"));

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokens)
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                        .ignoringRequestMatchers(SecurityConfig::hasBasicAuthorization))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/", "/index.html", "/assets/**", "/favicon.svg").permitAll()
                        .requestMatchers("/api/v1/auth/csrf", "/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().denyAll())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(apiAuthenticationEntryPoint)
                        .accessDeniedHandler(apiAccessDeniedHandler))
                .httpBasic(basic -> basic.authenticationEntryPoint(basicAuthenticationEntryPoint))
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT)));
        return http.build();
    }

    private static boolean hasBasicAuthorization(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authorization != null && authorization.regionMatches(true, 0, "Basic ", 0, 6);
    }

    private static void writeError(
            ObjectMapper objectMapper,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String message) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        objectMapper.writeValue(response.getOutputStream(), new ApiError(
                Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI()));
    }
}

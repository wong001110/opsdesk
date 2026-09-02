package com.wongjuenan.opsdesk.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.wongjuenan.opsdesk.security.OpsDeskPrincipal;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class BrowserSessionAuthenticationIntegrationTests {

    private static final String EMAIL = "browser@test.invalid";
    private static final String PASSWORD = "browser-test-password";
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000091");

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcClient jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void resetIdentity() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        jdbc.sql("delete from audit_event").update();
        jdbc.sql("delete from provider_profile").update();
        jdbc.sql("delete from ticket").update();
        jdbc.sql("delete from workspace_membership").update();
        jdbc.sql("delete from workspace").update();
        jdbc.sql("delete from app_user").update();
        jdbc.sql("""
                        insert into app_user (id, email, email_normalized, password_hash, display_name, enabled)
                        values (:id, :email, :email, :passwordHash, 'Browser User', true)
                        """)
                .param("id", USER_ID)
                .param("email", EMAIL)
                .param("passwordHash", passwordEncoder.encode(PASSWORD))
                .update();
    }

    @Test
    void browserLoginCreatesSessionAndSupportsMeThenLogoutInvalidatesIt() throws Exception {
        MvcResult csrfResult = csrf();
        Cookie csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        String csrfToken = csrfCookie.getValue();
        MockHttpSession anonymousSession = new MockHttpSession(context.getServletContext());
        String anonymousSessionId = anonymousSession.getId();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .session(anonymousSession)
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(content().string(not(containsString(PASSWORD))))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getId()).isNotEqualTo(anonymousSessionId);
        SecurityContext savedContext = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertThat(((OpsDeskPrincipal) savedContext.getAuthentication().getPrincipal()).getPassword()).isNull();

        mockMvc.perform(get("/api/v1/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Browser User"));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .session(session)
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfToken))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("JSESSIONID", 0))
                .andExpect(cookie().maxAge("XSRF-TOKEN", 0));
        assertThat(session.isInvalid()).isTrue();

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void browserMutationsRequireCsrfButExplicitBasicRemainsCliCompatible() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(EMAIL, PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));

        mockMvc.perform(post("/api/v1/workspaces")
                        .with(httpBasic(EMAIL, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slug\":\"cli-workspace\",\"name\":\"CLI Workspace\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void authenticationFailuresAreJsonAndNeverEchoCredentials() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.WWW_AUTHENTICATE))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/me"));

        MvcResult csrfResult = csrf();
        Cookie csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        String csrfToken = csrfCookie.getValue();
        String rejectedPassword = "SYNTHETIC_REJECTED_PASSWORD";
        mockMvc.perform(post("/api/v1/auth/login")
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(EMAIL, rejectedPassword)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(content().string(not(containsString(rejectedPassword))));

        mockMvc.perform(get("/api/v1/auth/me").with(httpBasic(EMAIL, rejectedPassword)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("OpsDesk")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(not(containsString(rejectedPassword))));
    }

    private MvcResult csrf() throws Exception {
        return mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
    }

    private String loginRequest(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

}

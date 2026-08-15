package com.nzube.bookingsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nzube.bookingsystem.repo.RefreshTokenRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class RefreshTokenFlowTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    private static final String BASE = "/api/v1/users/auth";
    private String testEmail;
    private String testPassword;

    @BeforeEach
    void setUp() throws Exception {
        // unique email per test run to avoid collisions if rollback doesn't fully isolate
        testEmail = "testuser_" + System.nanoTime() + "@example.com";
        testPassword = "Password123!";

        Map<String, String> registerBody = new HashMap<>();
        registerBody.put("name", "Test User");
        registerBody.put("email", testEmail);
        registerBody.put("password", testPassword);

        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated());
    }

    private MvcResult login() throws Exception {
        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("email", testEmail);
        loginBody.put("password", testPassword);

        return mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();
    }

    // ---------- LOGIN ----------

    @Test
    void login_returnsAccessTokenAndSetsRefreshCookie() throws Exception {
        MvcResult result = login();

        String accessToken = result.getResponse().getContentAsString();
        Cookie refreshCookie = result.getResponse().getCookie("refreshToken");

        assertNotNull(accessToken);
        assertFalse(accessToken.isBlank());
        assertNotNull(refreshCookie);
        assertTrue(refreshCookie.isHttpOnly());
        assertFalse(refreshCookie.getValue().isBlank());
    }

    // ---------- REFRESH / ROTATION ----------

    @Test
    void refresh_withValidToken_rotatesAndReturnsNewTokens() throws Exception {
        MvcResult loginResult = login();
        String oldRefreshToken = loginResult.getResponse().getCookie("refreshToken").getValue();
        String oldAccessToken = loginResult.getResponse().getContentAsString();

        MvcResult refreshResult = mockMvc.perform(post(BASE + "/refresh")
                        .cookie(new Cookie("refreshToken", oldRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        String newAccessToken = refreshResult.getResponse().getContentAsString();
        String newRefreshToken = refreshResult.getResponse().getCookie("refreshToken").getValue();

        assertNotEquals(oldAccessToken, newAccessToken);
        assertNotEquals(oldRefreshToken, newRefreshToken);
    }

    @Test
    void refresh_withUnknownToken_returns401() throws Exception {
        mockMvc.perform(post(BASE + "/refresh")
                        .cookie(new Cookie("refreshToken", "totally-fake-token")))
                .andExpect(status().isUnauthorized());
    }

    // ---------- REUSE DETECTION ----------

    @Test
    void refresh_reusingAlreadyRotatedToken_revokesAllSessionsForUser() throws Exception {
        MvcResult loginResult = login();
        String firstRefreshToken = loginResult.getResponse().getCookie("refreshToken").getValue();

        // first refresh: rotates firstRefreshToken -> secondRefreshToken (valid)
        MvcResult firstRefresh = mockMvc.perform(post(BASE + "/refresh")
                        .cookie(new Cookie("refreshToken", firstRefreshToken)))
                .andExpect(status().isOk())
                .andReturn();
        String secondRefreshToken = firstRefresh.getResponse().getCookie("refreshToken").getValue();

        // replay the now-revoked firstRefreshToken -> should trigger reuse detection
        mockMvc.perform(post(BASE + "/refresh")
                        .cookie(new Cookie("refreshToken", firstRefreshToken)))
                .andExpect(status().isUnauthorized());

        // secondRefreshToken was valid a moment ago, but reuse detection should
        // have revoked it too as part of family-wide revocation
        mockMvc.perform(post(BASE + "/refresh")
                        .cookie(new Cookie("refreshToken", secondRefreshToken)))
                .andExpect(status().isUnauthorized());
    }

    // ---------- EXPIRY ----------

    @Test
    void refresh_withExpiredToken_returns401() throws Exception {
        MvcResult loginResult = login();
        String rawToken = loginResult.getResponse().getCookie("refreshToken").getValue();

        // manually force expiry in the DB rather than waiting 7 real days
        var tokenHash = hashTokenForTest(rawToken);
        var storedToken = refreshTokenRepo.findByTokenHash(tokenHash).orElseThrow();
        storedToken.setExpiresAt(Instant.now().minusSeconds(60));
        refreshTokenRepo.save(storedToken);

        mockMvc.perform(post(BASE + "/refresh")
                        .cookie(new Cookie("refreshToken", rawToken)))
                .andExpect(status().isUnauthorized());
    }

    // ---------- LOGOUT ----------

    @Test
    void logout_revokesToken_soSubsequentRefreshFails() throws Exception {
        MvcResult loginResult = login();
        String rawToken = loginResult.getResponse().getCookie("refreshToken").getValue();

        mockMvc.perform(delete(BASE + "/logout")
                        .cookie(new Cookie("refreshToken", rawToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post(BASE + "/refresh")
                        .cookie(new Cookie("refreshToken", rawToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_isIdempotent_secondCallStillSucceeds() throws Exception {
        MvcResult loginResult = login();
        String rawToken = loginResult.getResponse().getCookie("refreshToken").getValue();

        mockMvc.perform(delete(BASE + "/logout")
                        .cookie(new Cookie("refreshToken", rawToken)))
                .andExpect(status().isNoContent());

        // same token again — should not error, just no-op
        mockMvc.perform(delete(BASE + "/logout")
                        .cookie(new Cookie("refreshToken", rawToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_withNoCookie_stillReturns204() throws Exception {
        mockMvc.perform(delete(BASE + "/logout"))
                .andExpect(status().isNoContent());
    }

    // ---------- helper: mirrors RefreshTokenService#hashToken ----------
    // duplicated here deliberately so the test doesn't depend on service internals being public;
    // if hashToken's algorithm ever changes, this test will fail loudly, which is desired.
    private String hashTokenForTest(String token) throws Exception {
        var md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return java.util.HexFormat.of().formatHex(hash);
    }
}
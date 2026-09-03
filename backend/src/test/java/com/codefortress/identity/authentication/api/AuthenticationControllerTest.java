package com.codefortress.identity.authentication.api;

import com.codefortress.identity.registration.RegisterUserCommand;
import com.codefortress.identity.registration.RegisterUserService;
import com.codefortress.identity.authentication.RefreshSessionService;
import com.codefortress.identity.authentication.refresh.InvalidRefreshTokenException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegisterUserService registerUserService;

    @Autowired
    private RefreshSessionService refreshSessionService;

    @Test
    void shouldAuthenticateUser() throws Exception {
        registerUser();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "  KAYQUE@EXAMPLE.COM ",
                                  "password": "correct-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("codefortress_refresh=")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("HttpOnly")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("SameSite=Strict")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("Path=/api/v1/auth")
                ))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.user.id").isNotEmpty())
                .andExpect(jsonPath("$.user.displayName")
                        .value("Kayque Miguel"))
                .andExpect(jsonPath("$.user.email")
                        .value("kayque@example.com"));
    }

    @Test
    void shouldRefreshAuthenticatedSession() throws Exception {
        registerUser();

        MvcResult loginResult = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "kayque@example.com",
                                          "password": "correct-password"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andReturn();

        Cookie initialRefreshCookie =
                extractRefreshCookie(loginResult);

        MvcResult refreshResult = mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .cookie(initialRefreshCookie)
                )
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("codefortress_refresh=")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("HttpOnly")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("SameSite=Strict")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("Path=/api/v1/auth")
                ))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.user.id").isNotEmpty())
                .andExpect(jsonPath("$.user.email")
                        .value("kayque@example.com"))
                .andReturn();

        Cookie rotatedRefreshCookie =
                extractRefreshCookie(refreshResult);

        assertThat(rotatedRefreshCookie.getValue())
                .isNotEqualTo(initialRefreshCookie.getValue());
    }

    @Test
    void shouldLogoutAndRevokeRefreshToken() throws Exception {
        registerUser();

        MvcResult loginResult = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "email": "kayque@example.com",
                                      "password": "correct-password"
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = extractRefreshCookie(loginResult);

        mockMvc.perform(
                        post("/api/v1/auth/logout")
                                .cookie(refreshCookie)
                )
                .andExpect(status().isNoContent())
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("codefortress_refresh=")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("Max-Age=0")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("HttpOnly")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("Path=/api/v1/auth")
                ));

        assertThatThrownBy(() ->
                refreshSessionService.refresh(
                        refreshCookie.getValue()
                )
        ).isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        registerUser();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "kayque@example.com",
                                  "password": "incorrect-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"));
    }

    @Test
    void shouldRejectInvalidLoginRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    private Cookie extractRefreshCookie(MvcResult result) {
        String setCookie = result
                .getResponse()
                .getHeader(HttpHeaders.SET_COOKIE);

        assertThat(setCookie).isNotBlank();

        String prefix = "codefortress_refresh=";

        assertThat(setCookie).startsWith(prefix);

        int valueEnd = setCookie.indexOf(';');

        String rawRefreshToken = setCookie.substring(
                prefix.length(),
                valueEnd
        );

        return new Cookie(
                "codefortress_refresh",
                rawRefreshToken
        );
    }

    private void registerUser() {
        registerUserService.register(
                new RegisterUserCommand(
                        "Kayque Miguel",
                        "kayque@example.com",
                        "correct-password"
                )
        );
    }
}
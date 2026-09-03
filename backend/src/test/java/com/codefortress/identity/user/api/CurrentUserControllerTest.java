package com.codefortress.identity.user.api;

import com.codefortress.identity.authentication.LoginCommand;
import com.codefortress.identity.authentication.LoginResult;
import com.codefortress.identity.authentication.LoginService;
import com.codefortress.identity.registration.RegisterUserCommand;
import com.codefortress.identity.registration.RegisterUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CurrentUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegisterUserService registerUserService;

    @Autowired
    private LoginService loginService;

    @Test
    void shouldReturnAuthenticatedUser() throws Exception {
        registerUserService.register(
                new RegisterUserCommand(
                        "Kayque Miguel",
                        "kayque@example.com",
                        "correct-password"
                )
        );

        LoginResult loginResult = loginService.login(
                new LoginCommand(
                        "kayque@example.com",
                        "correct-password"
                )
        );

        mockMvc.perform(get("/api/v1/users/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer "
                                        + loginResult
                                        .accessToken()
                                        .value()
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(loginResult.userId().toString()))
                .andExpect(jsonPath("$.displayName")
                        .value("Kayque Miguel"))
                .andExpect(jsonPath("$.email")
                        .value("kayque@example.com"));
    }

    @Test
    void shouldRejectRequestWithoutAccessToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidAccessToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer invalid-token"
                        ))
                .andExpect(status().isUnauthorized());
    }
}
package com.codefortress.identity.registration.api;

import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldRegisterUser() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "Kayque Miguel",
                                  "email": "KAYQUE@EXAMPLE.COM",
                                  "password": "a-strong-password"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.displayName")
                        .value("Kayque Miguel"))
                .andExpect(jsonPath("$.email")
                        .value("kayque@example.com"));

        User persistedUser = userRepository
                .findByEmail("kayque@example.com")
                .orElseThrow();

        assertThat(persistedUser.getPasswordHash())
                .isNotEqualTo("a-strong-password");

        assertThat(passwordEncoder.matches(
                "a-strong-password",
                persistedUser.getPasswordHash()
        )).isTrue();
    }

    @Test
    void shouldRejectDuplicatedEmail() throws Exception {
        String firstRequest = """
                {
                  "displayName": "Kayque Miguel",
                  "email": "kayque@example.com",
                  "password": "first-password"
                }
                """;

        String duplicatedRequest = """
                {
                  "displayName": "Outro usuário",
                  "email": "  KAYQUE@EXAMPLE.COM  ",
                  "password": "second-password"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicatedRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("EMAIL_ALREADY_REGISTERED"));

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldRejectInvalidRegistration() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "",
                                  "email": "invalid-email",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.displayName")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.email")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.password")
                        .exists());
    }
}
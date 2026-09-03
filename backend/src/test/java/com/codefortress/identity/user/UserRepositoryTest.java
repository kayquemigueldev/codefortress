package com.codefortress.identity.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPersistAndFindUserByNormalizedEmail() {
        User user = User.create(
                "  KAYQUE@EXAMPLE.COM  ",
                "test-password-hash",
                "Kayque Miguel"
        );

        userRepository.saveAndFlush(user);

        User persistedUser = userRepository
                .findByEmail("kayque@example.com")
                .orElseThrow();

        assertThat(persistedUser.getId()).isNotNull();
        assertThat(persistedUser.getEmail()).isEqualTo("kayque@example.com");
        assertThat(persistedUser.getDisplayName()).isEqualTo("Kayque Miguel");
        assertThat(persistedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(persistedUser.getCreatedAt()).isNotNull();
        assertThat(persistedUser.getUpdatedAt()).isNotNull();
        assertThat(userRepository.existsByEmail("kayque@example.com")).isTrue();
    }
}
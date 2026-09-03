package com.codefortress.identity.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CurrentUser get(UUID userId) {
        User user = userRepository
                .findById(userId)
                .filter(foundUser ->
                        foundUser.getStatus() == UserStatus.ACTIVE
                )
                .orElseThrow(CurrentUserUnavailableException::new);

        return new CurrentUser(
                user.getId(),
                user.getDisplayName(),
                user.getEmail()
        );
    }
}
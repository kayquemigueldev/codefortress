package com.codefortress.identity.authentication;

import com.codefortress.identity.authentication.refresh.IssuedRefreshToken;
import com.codefortress.identity.authentication.refresh.RefreshTokenService;
import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.identity.user.UserStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final String dummyPasswordHash;

    public LoginService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AccessTokenService accessTokenService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;

        this.dummyPasswordHash = passwordEncoder.encode(
                "codefortress-dummy-password"
        );
    }

    @Transactional
    public LoginResult login(LoginCommand command) {
        Optional<User> possibleUser = userRepository.findByEmail(
                command.email()
        );

        String passwordHash = possibleUser
                .map(User::getPasswordHash)
                .orElse(dummyPasswordHash);

        boolean passwordMatches = passwordEncoder.matches(
                command.password(),
                passwordHash
        );

        if (possibleUser.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        User user = possibleUser.get();

        if (!passwordMatches || user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException();
        }

        AccessToken accessToken = accessTokenService.issue(
                user.getId(),
                user.getEmail()
        );

        IssuedRefreshToken refreshToken =
                refreshTokenService.issue(user.getId());

        return new LoginResult(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                accessToken,
                refreshToken
        );
    }
}
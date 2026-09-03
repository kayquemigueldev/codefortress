package com.codefortress.identity.authentication;

import com.codefortress.identity.authentication.refresh.InvalidRefreshTokenException;
import com.codefortress.identity.authentication.refresh.RefreshTokenService;
import com.codefortress.identity.authentication.refresh.RotatedRefreshToken;
import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.identity.user.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshSessionService {

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final AccessTokenService accessTokenService;

    public RefreshSessionService(
            RefreshTokenService refreshTokenService,
            UserRepository userRepository,
            AccessTokenService accessTokenService
    ) {
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.accessTokenService = accessTokenService;
    }

    @Transactional(
            noRollbackFor = InvalidRefreshTokenException.class
    )
    public LoginResult refresh(String rawRefreshToken) {
        RotatedRefreshToken rotatedToken =
                refreshTokenService.rotate(rawRefreshToken);

        User user = userRepository
                .findById(rotatedToken.userId())
                .filter(foundUser ->
                        foundUser.getStatus() == UserStatus.ACTIVE
                )
                .orElse(null);

        if (user == null) {
            refreshTokenService.revoke(
                    rotatedToken.refreshToken().value()
            );

            throw new InvalidRefreshTokenException();
        }

        AccessToken accessToken = accessTokenService.issue(
                user.getId(),
                user.getEmail()
        );

        return new LoginResult(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                accessToken,
                rotatedToken.refreshToken()
        );
    }
}
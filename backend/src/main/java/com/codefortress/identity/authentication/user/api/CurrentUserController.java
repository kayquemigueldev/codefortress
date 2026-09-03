package com.codefortress.identity.user.api;

import com.codefortress.identity.user.CurrentUser;
import com.codefortress.identity.user.CurrentUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class CurrentUserController {

    private final CurrentUserService currentUserService;

    public CurrentUserController(
            CurrentUserService currentUserService
    ) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public CurrentUserResponse getCurrentUser(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        CurrentUser currentUser = currentUserService.get(userId);

        return CurrentUserResponse.from(currentUser);
    }
}
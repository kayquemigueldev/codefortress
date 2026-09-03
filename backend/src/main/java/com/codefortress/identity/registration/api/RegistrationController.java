package com.codefortress.identity.registration.api;

import com.codefortress.identity.registration.RegisterUserCommand;
import com.codefortress.identity.registration.RegisterUserService;
import com.codefortress.identity.registration.RegisteredUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class RegistrationController {

    private final RegisterUserService registerUserService;

    public RegistrationController(
            RegisterUserService registerUserService
    ) {
        this.registerUserService = registerUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> register(
            @Valid @RequestBody RegisterUserRequest request
    ) {
        RegisteredUser registeredUser = registerUserService.register(
                new RegisterUserCommand(
                        request.displayName(),
                        request.email(),
                        request.password()
                )
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RegisterUserResponse.from(registeredUser));
    }
}
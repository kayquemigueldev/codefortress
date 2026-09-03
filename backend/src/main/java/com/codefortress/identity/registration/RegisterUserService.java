package com.codefortress.identity.registration;

import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisteredUser register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyRegisteredException();
        }

        String passwordHash = passwordEncoder.encode(command.password());

        User user = User.create(
                command.email(),
                passwordHash,
                command.displayName()
        );

        User savedUser = userRepository.save(user);

        return new RegisteredUser(
                savedUser.getId(),
                savedUser.getDisplayName(),
                savedUser.getEmail()
        );
    }
}
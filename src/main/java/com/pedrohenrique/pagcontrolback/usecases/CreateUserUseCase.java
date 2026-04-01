package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.CreateUserCommand;
import com.pedrohenrique.pagcontrolback.exceptions.EmailAlreadyInUseException;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CreateUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User execute(CreateUserCommand command) {

        if(userRepository.existsUserByEmail((command.email()))) {
            throw new EmailAlreadyInUseException("Email already exists");
        }

        User user = new User(
                command.name(),
                command.fantasyName(),
                command.email(),
                passwordEncoder.encode(command.password()),
                command.phone(),
                command.personType()
        );

        return userRepository.save(user);
    }

}

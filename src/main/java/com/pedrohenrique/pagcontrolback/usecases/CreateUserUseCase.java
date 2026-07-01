package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Password;
import com.pedrohenrique.pagcontrolback.dtos.command.CreateUserCommand;
import com.pedrohenrique.pagcontrolback.dtos.events.UserCreatedEvent;
import com.pedrohenrique.pagcontrolback.exceptions.EmailAlreadyInUseException;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher publisher;

    public CreateUserUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher publisher
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.publisher = publisher;
    }

    @Transactional
    public User execute(CreateUserCommand command) {

        if(userRepository.existsUserByEmail((command.email()))) {
            throw new EmailAlreadyInUseException("Email already exists");
        }

        Password password = new Password(command.password());

        User user = new User(
                command.name(),
                command.fantasyName(),
                command.email(),
                passwordEncoder.encode(password.value()),
                command.phone(),
                command.personType()
        );

        user = userRepository.save(user);

       publisher.publishEvent(new UserCreatedEvent(
               user.getId(),
               user.getName(),
               user.getEmail().value()
       ));

        return user;
    }

}

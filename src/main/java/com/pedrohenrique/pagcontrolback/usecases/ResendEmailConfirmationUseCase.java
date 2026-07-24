package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Email;
import com.pedrohenrique.pagcontrolback.dtos.events.ConfirmationEmailEvent;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ResendEmailConfirmationUseCase {

    private final ApplicationEventPublisher publisher;
    private final UserRepository userRepository;

    public ResendEmailConfirmationUseCase(ApplicationEventPublisher publisher, UserRepository userRepository) {
        this.publisher = publisher;
        this.userRepository = userRepository;
    }

    public void execute(String email) {
        Email emailObject = new Email(email);

        Optional<User> optionalUser = userRepository.findByEmail(emailObject.value());

        if (optionalUser.isPresent() && !optionalUser.get().isEmailVerified()) {

            User user = optionalUser.get();

            publisher.publishEvent(
                    new ConfirmationEmailEvent(
                            user.getId(),
                            user.getName(),
                            user.getEmail().value()
                    )
            );
        }

    }
}

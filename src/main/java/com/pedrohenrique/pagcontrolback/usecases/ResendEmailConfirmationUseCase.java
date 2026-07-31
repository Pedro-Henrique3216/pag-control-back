package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Email;
import com.pedrohenrique.pagcontrolback.dtos.events.ConfirmationEmailEvent;
import com.pedrohenrique.pagcontrolback.exceptions.ResendConfirmationLimitException;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Optional;

@Service
public class ResendEmailConfirmationUseCase {

    private final ApplicationEventPublisher publisher;
    private final UserRepository userRepository;
    private final Clock clock;

    public ResendEmailConfirmationUseCase(ApplicationEventPublisher publisher, UserRepository userRepository, Clock clock) {
        this.publisher = publisher;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public void execute(String email) {
        Email emailObject = new Email(email);

        Optional<User> optionalUser = userRepository.findByEmail(emailObject.value());

        if (optionalUser.isPresent() && !optionalUser.get().isEmailVerified()) {

            User user = optionalUser.get();

            if (!user.canResendConfirmationEmail(clock)) {
                throw new ResendConfirmationLimitException(
                        "Confirmation email resend limit exceeded"
                );
            }

            user.registerConfirmationEmailSent(clock);

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

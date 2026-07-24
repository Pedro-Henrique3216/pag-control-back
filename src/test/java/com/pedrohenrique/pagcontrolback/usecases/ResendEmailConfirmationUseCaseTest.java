package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.events.ConfirmationEmailEvent;
import com.pedrohenrique.pagcontrolback.model.PersonType;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResendEmailConfirmationUseCaseTest {

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ResendEmailConfirmationUseCase useCase;

    @Test
    void shouldPublishConfirmationEmailEvent() {

        User user = new User(
                "Pedro",
                null,
                "pedro@gmail.com",
                "123456",
                "11999999999",
                PersonType.PF
        );

        when(userRepository.findByEmail("pedro@gmail.com"))
                .thenReturn(Optional.of(user));

        useCase.execute("pedro@gmail.com");

        ArgumentCaptor<ConfirmationEmailEvent> captor =
                ArgumentCaptor.forClass(ConfirmationEmailEvent.class);

        verify(publisher).publishEvent(captor.capture());

        ConfirmationEmailEvent event = captor.getValue();

        assertEquals(user.getId(), event.userId());
        assertEquals(user.getName(), event.name());
        assertEquals(user.getEmail().value(), event.email());
    }

    @Test
    void shouldNotPublishEventWhenUserDoesNotExist() {

        when(userRepository.findByEmail("pedro@gmail.com"))
                .thenReturn(Optional.empty());

        useCase.execute("pedro@gmail.com");

        verifyNoInteractions(publisher);
    }

    @Test
    void shouldNotPublishEventWhenEmailIsAlreadyVerified() {

        User user = new User(
                "Pedro",
                null,
                "pedro@gmail.com",
                "123456",
                "11999999999",
                PersonType.PF
        );

        user.verifyEmail();

        when(userRepository.findByEmail("pedro@gmail.com"))
                .thenReturn(Optional.of(user));

        useCase.execute("pedro@gmail.com");

        verify(publisher, never()).publishEvent(any());
    }
}
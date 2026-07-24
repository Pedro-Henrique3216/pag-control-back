package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.config.security.TokenService;
import com.pedrohenrique.pagcontrolback.exceptions.InvalidConfirmationTokenException;
import com.pedrohenrique.pagcontrolback.model.PersonType;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyEmailUseCaseTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VerifyEmailUseCase useCase;


    @Test
    void shouldVerifyEmailSuccessfully() {

        UUID userId = UUID.randomUUID();

        User user = new User(
                "Pedro",
                null,
                "pedro@gmail.com",
                "123456",
                "11999999999",
                PersonType.PF
        );

        when(tokenService.getUserId("token123"))
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));


        useCase.execute("token123");


        assertTrue(user.isEmailVerified());
    }


    @Test
    void shouldNotChangeWhenEmailIsAlreadyVerified() {

        UUID userId = UUID.randomUUID();

        User user = new User(
                "Pedro",
                null,
                "pedro@gmail.com",
                "123456",
                "11999999999",
                PersonType.PF
        );

        user.verifyEmail();

        when(tokenService.getUserId("token123"))
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));


        useCase.execute("token123");


        assertTrue(user.isEmailVerified());

        verify(userRepository)
                .findById(userId);
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {

        UUID userId = UUID.randomUUID();

        when(tokenService.getUserId("invalid-token"))
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());


        assertThrows(
                InvalidConfirmationTokenException.class,
                () -> useCase.execute("invalid-token")
        );
    }
}
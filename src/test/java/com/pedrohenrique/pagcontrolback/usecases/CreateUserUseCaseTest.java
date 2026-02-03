package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.model.PersonType;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreateUserUseCase createUserUseCase;

    private final User user = new User(
            "John Doe",
            "JD Supplies",
            "test@gmail.com",
            "12345678",
            "111-222-3333",
            PersonType.PJ
    );

    @Test
    void whenUserAlreadyExists_shouldThrowEmailAlreadyInUseException() {

        when(userRepository.existsUserByEmail(user.getEmail())).thenReturn(true);

        assertThrows(
                com.pedrohenrique.pagcontrolback.exceptions.EmailAlreadyInUseException.class,
                () -> createUserUseCase.execute(user)
        );
    }

    @Test
    void whenUserDoesNotExist_shouldCreateUserSuccessfully() {

        when(userRepository.existsUserByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User createdUser = createUserUseCase.execute(user);

        verify(userRepository, times(1)).save(user);

        assertNotNull(createdUser);
        assertEquals(user.getEmail(), createdUser.getEmail());
    }


}
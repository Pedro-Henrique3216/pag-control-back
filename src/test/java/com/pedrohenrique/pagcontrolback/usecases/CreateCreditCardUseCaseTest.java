
package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.CreateCreditCardCommand;
import com.pedrohenrique.pagcontrolback.exceptions.InvalidCreditCardClosingDayException;
import com.pedrohenrique.pagcontrolback.model.CreditCard;
import com.pedrohenrique.pagcontrolback.model.PersonType;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.CreditCardRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCreditCardUseCaseTest {

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreateCreditCardUseCase createCreditCardUseCase;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "$2a$10$abcdefghijklmnopqrstuv",
                "11999999999",
                PersonType.PF
        );
    }

    @Test
    void shouldCreateCreditCardSuccessfully() {
        CreateCreditCardCommand command = new CreateCreditCardCommand(
                "Nubank",
                10,
                17,
                new BigDecimal("5000.00")
        );

        when(userRepository.getReferenceById(userId)).thenReturn(user);

        when(creditCardRepository.save(any(CreditCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreditCard result = createCreditCardUseCase.execute(command, userId);

        assertNotNull(result);
        assertEquals("Nubank", result.getName());
        assertEquals(10, result.getClosingDay());
        assertEquals(17, result.getDueDay());
        assertEquals(
                new BigDecimal("5000.00"),
                result.getCreditLimit().value()
        );
        assertEquals(user, result.getUser());
        assertTrue(result.isActive());

        verify(userRepository).getReferenceById(userId);
        verify(creditCardRepository).save(any(CreditCard.class));
    }

    @Test
    void shouldAssociateCreditCardWithAuthenticatedUser() {
        CreateCreditCardCommand command = new CreateCreditCardCommand(
                "Nubank",
                10,
                17,
                new BigDecimal("5000.00")
        );

        when(userRepository.getReferenceById(userId)).thenReturn(user);

        when(creditCardRepository.save(any(CreditCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        createCreditCardUseCase.execute(command, userId);

        ArgumentCaptor<CreditCard> captor =
                ArgumentCaptor.forClass(CreditCard.class);

        verify(creditCardRepository).save(captor.capture());

        assertSame(user, captor.getValue().getUser());
    }

    @Test
    void shouldSaveCreditCardOnlyOnce() {
        CreateCreditCardCommand command = new CreateCreditCardCommand(
                "Nubank",
                10,
                17,
                new BigDecimal("5000.00")
        );

        when(userRepository.getReferenceById(userId)).thenReturn(user);

        when(creditCardRepository.save(any(CreditCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        createCreditCardUseCase.execute(command, userId);

        verify(creditCardRepository, times(1)).save(any(CreditCard.class));
    }

    @Test
    void shouldThrowWhenClosingDayIsInvalid() {
        CreateCreditCardCommand command = new CreateCreditCardCommand(
                "Nubank",
                32,
                17,
                new BigDecimal("5000.00")
        );

        when(userRepository.getReferenceById(userId)).thenReturn(user);

        assertThrows(
                InvalidCreditCardClosingDayException.class,
                () -> createCreditCardUseCase.execute(command, userId)
        );

        verify(creditCardRepository, never()).save(any(CreditCard.class));
    }
}

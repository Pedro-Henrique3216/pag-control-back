package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.exceptions.InstallmentAccessDeniedException;
import com.pedrohenrique.pagcontrolback.exceptions.InstallmentNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayInstallmentUseCaseTest {

    @Mock
    private InstallmentRepository installmentRepository;

    @InjectMocks
    private PayInstallmentUseCase payInstallmentUseCase;

    private User createUser() {

        User user = new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "password123",
                "11999999999",
                PersonType.PF
        );

        ReflectionTestUtils.setField(
                user,
                "id",
                UUID.randomUUID()
        );

        return user;
    }

    private Installment createInstallment(User user) {

        Expense expense = new Expense(
                "INV-001",
                "Test Expense",
                PaymentType.CREDIT,
                LocalDate.now(),
                user,
                null
        );

        return new Installment(
                Money.of(BigDecimal.valueOf(200)),
                LocalDate.now().plusDays(10),
                "12345678901234567890123456789012345678901234",
                expense,
                1,
                1
        );
    }

    @Nested
    class SuccessTests {

        @Test
        void shouldMarkInstallmentAsPaidWhenUserAndInstallmentAreValid() {

            User user = createUser();

            Installment installment = createInstallment(user);

            when(installmentRepository.findById(any()))
                    .thenReturn(Optional.of(installment));

            payInstallmentUseCase.execute(
                    user.getId(),
                    UUID.randomUUID()
            );

            verify(installmentRepository, times(1))
                    .save(installment);

            assertEquals(
                    InstallmentStatus.PAID,
                    installment.getStatus()
            );
        }
    }

    @Nested
    class ValidationTests {

        @Test
        void shouldThrowUserRequiredExceptionWhenUserIdIsNull() {

            assertThrows(
                    UserRequiredException.class,
                    () -> payInstallmentUseCase.execute(
                            null,
                            UUID.randomUUID()
                    )
            );
        }

        @Test
        void shouldThrowInstallmentNotFoundExceptionWhenInstallmentDoesNotExist() {

            when(installmentRepository.findById(any()))
                    .thenReturn(Optional.empty());

            assertThrows(
                    InstallmentNotFoundException.class,
                    () -> payInstallmentUseCase.execute(
                            UUID.randomUUID(),
                            UUID.randomUUID()
                    )
            );
        }

        @Test
        void shouldThrowInstallmentAccessDeniedExceptionWhenInstallmentDoesNotBelongToUser() {

            User installmentOwner = createUser();

            User anotherUser = createUser();

            Installment installment = createInstallment(installmentOwner);

            when(installmentRepository.findById(any()))
                    .thenReturn(Optional.of(installment));

            assertThrows(
                    InstallmentAccessDeniedException.class,
                    () -> payInstallmentUseCase.execute(
                            anotherUser.getId(),
                            UUID.randomUUID()
                    )
            );

            verify(installmentRepository, never())
                    .save(any());
        }
    }
}
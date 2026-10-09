package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.events.InstallmentReminderEvent;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendInstallmentReminderUseCaseTest {

    @Mock
    private InstallmentRepository installmentRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @InjectMocks
    private SendInstallmentReminderUseCase sendInstallmentReminderUseCase;

    @Test
    void shouldPublishReminderEventForUserWithPendingInstallments(){
        User user = new User(
                "Pedro",
                null,
                "pedro@gmail.com",
                "123456",
                "11999999999",
                PersonType.PF
        );

        CreditCard creditCard = new CreditCard(
                "NUBANK",
                9,
                15,
                Money.of(BigDecimal.valueOf(1000.00)),
                new User()
        );

        Expense expense = new Expense(
                "1234",
                "teste",
                PaymentType.CREDIT,
                LocalDate.now(),
                user,
                Money.of(BigDecimal.valueOf(300)),
                creditCard
        );

        expense.generateCreditCardInstallments(1);
        List<Installment> installments = expense.getInstallments();
        when(installmentRepository.findPendingInstallmentsUntil(LocalDate.now().plusDays(7))).thenReturn(installments);
        sendInstallmentReminderUseCase.execute();
        verify(applicationEventPublisher).publishEvent(any(InstallmentReminderEvent.class));
    }

    @Test
    void shouldPublishOneReminderEventPerUser(){
        User user = new User(
                "Pedro",
                null,
                "pedro@gmail.com",
                "123456",
                "11999999999",
                PersonType.PF
        );

        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        CreditCard creditCard = new CreditCard(
                "NUBANK",
                9,
                15,
                Money.of(BigDecimal.valueOf(1000.00)),
                new User()
        );

        Expense expense = new Expense(
                "1234",
                "teste",
                PaymentType.CREDIT,
                LocalDate.now(),
                user,
                Money.of(BigDecimal.valueOf(300)),
                creditCard
        );

        expense.generateCreditCardInstallments(
                3
        );

        User user2 = new User(
                "Pedro",
                null,
                "pedro2@gmail.com",
                "123456",
                "11999999999",
                PersonType.PF
        );

        ReflectionTestUtils.setField(user2, "id", UUID.randomUUID());

        CreditCard creditCard2 = new CreditCard(
                "NUBANK",
                9,
                15,
                Money.of(BigDecimal.valueOf(1000.00)),
                new User()
        );

        Expense expense2 = new Expense(
                "12343",
                "teste2",
                PaymentType.CREDIT,
                LocalDate.now(),
                user2,
                Money.of(BigDecimal.valueOf(300)),
                creditCard2
        );

        expense2.generateCreditCardInstallments(
                2
        );

        List<Installment> installments = new ArrayList<>(expense.getInstallments());
        installments.addAll(expense2.getInstallments());

        when(installmentRepository.findPendingInstallmentsUntil(LocalDate.now().plusDays(7))).thenReturn(installments);
        sendInstallmentReminderUseCase.execute();
        verify(applicationEventPublisher, times(2)).publishEvent(any(InstallmentReminderEvent.class));
    }

    @Test
    void shouldSeparateOverdueAndUpcomingInstallments(){
        User user = new User(
                "Pedro",
                null,
                "pedro@gmail.com",
                "123456",
                "11999999999",
                PersonType.PF
        );


        CreditCard creditCard = new CreditCard(
                "NUBANK",
                9,
                15,
                Money.of(BigDecimal.valueOf(1000.00)),
                new User()
        );

        Expense expense = new Expense(
                "1234",
                "teste",
                PaymentType.CREDIT,
                LocalDate.now().minusMonths(1),
                user,
                Money.of(BigDecimal.valueOf(300)),
                creditCard
        );

        expense.generateCreditCardInstallments(
                3
        );

        List<Installment> installments = expense.getInstallments();
        when(installmentRepository.findPendingInstallmentsUntil(LocalDate.now().plusDays(7))).thenReturn(List.of(installments.get(0),  installments.get(1)));

        sendInstallmentReminderUseCase.execute();
        ArgumentCaptor<InstallmentReminderEvent> captor = ArgumentCaptor.forClass(InstallmentReminderEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        InstallmentReminderEvent event = captor.getValue();

        assertEquals(1, event.upcoming().size());
        assertEquals(1, event.overdue().size());

    }

    @Test
    void shouldNotPublishEventWhenThereAreNoPendingInstallments(){

        when(installmentRepository.findPendingInstallmentsUntil(any(LocalDate.class))).thenReturn(Collections.emptyList());
        sendInstallmentReminderUseCase.execute();
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldIncludeInstallmentDataInReminderEvent(){
        User user = new User(
                "Pedro",
                null,
                "pedro@gmail.com",
                "123456",
                "11999999999",
                PersonType.PF
        );

        CreditCard creditCard = new CreditCard(
                "NUBANK",
                9,
                15,
                Money.of(BigDecimal.valueOf(1000.00)),
                new User()
        );

        Expense expense = new Expense(
                "1234",
                "teste",
                PaymentType.CREDIT,
                LocalDate.now().minusMonths(1),
                user,
                Money.of(BigDecimal.valueOf(300)),
                creditCard
        );

        expense.generateCreditCardInstallments(
                1
        );

        List<Installment> installments = expense.getInstallments();
        when(installmentRepository.findPendingInstallmentsUntil(LocalDate.now().plusDays(7))).thenReturn(installments);

        sendInstallmentReminderUseCase.execute();
        ArgumentCaptor<InstallmentReminderEvent> captor = ArgumentCaptor.forClass(InstallmentReminderEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        InstallmentReminderEvent event = captor.getValue();

        assertEquals("teste", event.overdue().get(0).description());
        assertEquals(BigDecimal.valueOf(300).setScale(2, RoundingMode.HALF_UP), event.overdue().get(0).amount());
        assertEquals(user.getName(), event.name());
        assertEquals(user.getEmail().value(), event.email());
    }

}
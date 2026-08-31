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

        Expense expense = new Expense(
                "1234",
                "teste",
                PaymentType.CREDIT,
                LocalDate.now(),
                user,
                Money.of(BigDecimal.valueOf(300))
        );

        expense.generateInstallments(
                Map.of(3, "TESTE!@#")
        );
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

        Expense expense = new Expense(
                "1234",
                "teste",
                PaymentType.CREDIT,
                LocalDate.now(),
                user,
                Money.of(BigDecimal.valueOf(300))
        );

        expense.generateInstallments(
                Map.of(3, "TESTE!@#", 4, "", 6, "")
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

        Expense expense2 = new Expense(
                "12343",
                "teste2",
                PaymentType.CREDIT,
                LocalDate.now(),
                user2,
                Money.of(BigDecimal.valueOf(300))
        );

        expense2.generateInstallments(
                Map.of(3, "TESTE!@#", 6, "")
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

        Expense expense = new Expense(
                "1234",
                "teste",
                PaymentType.CREDIT,
                LocalDate.now().minusDays(3),
                user,
                Money.of(BigDecimal.valueOf(300))
        );

        expense.generateInstallments(
                Map.of(2, "TESTE!@#", 7, "", 10, "")
        );

        List<Installment> installments = expense.getInstallments();
        when(installmentRepository.findPendingInstallmentsUntil(LocalDate.now().plusDays(7))).thenReturn(installments);

        sendInstallmentReminderUseCase.execute();
        ArgumentCaptor<InstallmentReminderEvent> captor = ArgumentCaptor.forClass(InstallmentReminderEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        InstallmentReminderEvent event = captor.getValue();

        assertEquals(2, event.upcoming().size());
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

        Expense expense = new Expense(
                "1234",
                "teste",
                PaymentType.CREDIT,
                LocalDate.now().minusDays(3),
                user,
                Money.of(BigDecimal.valueOf(300))
        );

        expense.generateInstallments(
                Map.of(2, "TESTE!@#")
        );

        List<Installment> installments = expense.getInstallments();
        when(installmentRepository.findPendingInstallmentsUntil(LocalDate.now().plusDays(7))).thenReturn(installments);

        sendInstallmentReminderUseCase.execute();
        ArgumentCaptor<InstallmentReminderEvent> captor = ArgumentCaptor.forClass(InstallmentReminderEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        InstallmentReminderEvent event = captor.getValue();

        assertEquals("teste", event.overdue().get(0).description());
        assertEquals(BigDecimal.valueOf(300).setScale(2, RoundingMode.HALF_UP), event.overdue().get(0).amount());
        assertEquals("TESTE!@#", event.overdue().get(0).barcode());
        assertEquals(user.getName(), event.name());
        assertEquals(user.getEmail().value(), event.email());
    }

}
package com.pedrohenrique.pagcontrolback.schedulers;

import com.pedrohenrique.pagcontrolback.usecases.SendInstallmentReminderUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SendInstallmentReminderSchedulerTest {

    @Mock
    private SendInstallmentReminderUseCase useCase;

    @InjectMocks
    private SendInstallmentReminderScheduler scheduler;

    @Test
    void shouldExecuteSendInstallmentReminderUseCase() {

        scheduler.execute();

        verify(useCase).execute();
    }
}
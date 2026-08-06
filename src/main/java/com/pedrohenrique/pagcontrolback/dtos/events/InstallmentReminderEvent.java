package com.pedrohenrique.pagcontrolback.dtos.events;

import com.pedrohenrique.pagcontrolback.dtos.response.InstallmentReminderData;

import java.util.List;

public record InstallmentReminderEvent(
        String name,
        String email,
        List<InstallmentReminderData> overdue,
        List<InstallmentReminderData> upcoming
) {
}

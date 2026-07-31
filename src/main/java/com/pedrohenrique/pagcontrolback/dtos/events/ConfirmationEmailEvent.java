package com.pedrohenrique.pagcontrolback.dtos.events;

import java.util.UUID;

public record ConfirmationEmailEvent(
        UUID userId,
        String name,
        String email
) {
}

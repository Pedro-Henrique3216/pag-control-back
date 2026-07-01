package com.pedrohenrique.pagcontrolback.dtos.events;

import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String name,
        String email
) {
}

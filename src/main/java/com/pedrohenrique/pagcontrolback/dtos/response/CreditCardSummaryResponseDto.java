package com.pedrohenrique.pagcontrolback.dtos.response;

import java.util.UUID;

public record CreditCardSummaryResponseDto(
        UUID id,
        String name
) {
}

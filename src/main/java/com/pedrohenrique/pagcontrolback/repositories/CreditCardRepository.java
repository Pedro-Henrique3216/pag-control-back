package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {

    @Query(
            """
                SELECT c FROM CreditCard c WHERE c.id = :cardId AND c.user.id = :userId
            """
    )
    CreditCard findCreditCardByCardIdAndUserId(UUID cardId, UUID userId);
}

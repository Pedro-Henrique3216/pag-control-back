package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.model.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IncomeRepository extends JpaRepository<Income, UUID> {

    @Query("""
        SELECT COALESCE(SUM(i.amount.value), 0)
            FROM Income i
                WHERE i.user.id = :userId
                    AND i.date
                        BETWEEN :startDate
                            AND :endDate
    """)
    BigDecimal sumByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);

    @Query(
            """
                SELECT i from Income i
                            WHERE i.user.id = :userId
                                        AND i.date = :date
            """
    )
    List<Income> findAllIncomeByUserIdAndDate(UUID userId, LocalDate date);

    @Query(
            """
                    SELECT i from Income i
                                WHERE i.user.id = :userId
                """
    )

    List<Income> findAllIncomeByUserId(UUID userId);
}

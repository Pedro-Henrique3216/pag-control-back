package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.dtos.request.ListInstallmentQuery;
import com.pedrohenrique.pagcontrolback.model.Installment;
import com.pedrohenrique.pagcontrolback.model.InstallmentStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class InstallmentRepositoryImpl implements InstallmentRepositoryCustom{

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Installment> search(ListInstallmentQuery query, UUID userId) {

            CriteriaBuilder builder = em.getCriteriaBuilder();

            CriteriaQuery<Installment> criteria = builder.createQuery(Installment.class);

            var installment = criteria.from(Installment.class);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    builder.equal(installment.get("expense").get("user").get("id"), userId)
            );

            if (query.supplierId() != null) {
                predicates.add(
                        builder.equal(installment.get("expense").get("supplier").get("id"), query.supplierId())
                );
            }

            if (query.month() != null) {
                predicates.add(
                        builder.equal(
                                builder.function(
                                        "to_char",
                                        String.class,
                                        installment.get("dueDate"),
                                        builder.literal("YYYY-MM")
                                ),
                                query.month().toString()
                        )
                );
            }

            if (query.status() != null) {
                predicates.add(
                        builder.equal(installment.get("status"), query.status())
                );
            }

            if (query.overdue() != null && query.overdue()) {
                predicates.add(
                        builder.and(
                                builder.lessThan(installment.get("dueDate"), builder.currentDate()),
                                builder.equal(installment.get("status"), InstallmentStatus.UNPAID)
                        )
                );
            }



            if (query.dueInNext7Days() != null && query.dueInNext7Days()) {
                LocalDate now = LocalDate.now();
                LocalDate next7 = now.plusDays(7);
                predicates.add(
                        builder.and(
                                builder.between(
                                        installment.get("dueDate"),
                                        now,
                                        next7
                                ),
                                builder.equal(installment.get("status"), InstallmentStatus.UNPAID)
                        )
                );
            }

            criteria.where(predicates.toArray(new Predicate[0]));

            return em.createQuery(criteria).getResultList();
    }
}

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
import java.time.YearMonth;
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

            if(query.search() != null){
                predicates.add(builder.or(
                        builder.like(
                                builder.lower(installment.get("expense").get("description")),
                                "%" + query.search().toLowerCase() + "%"
                        ),
                        builder.like(
                                builder.lower(installment.get("expense").get("invoiceNumber")),
                                "%" + query.search().toLowerCase() + "%"
                        )
                ));
            }

            if (query.supplierId() != null) {
                predicates.add(
                        builder.equal(installment.get("expense").get("supplier").get("id"), query.supplierId())
                );
            }

            if (query.month() != null) {

                YearMonth yearMonth = query.month();
                LocalDate start = yearMonth.atDay(1);
                LocalDate end = yearMonth.atEndOfMonth();
                predicates.add(
                        builder.between(
                                installment.get("dueDate"),
                                start,
                                end
                        )
                );
            }

            if (query.status() != null) {
                predicates.add(
                        builder.equal(installment.get("status"), query.status())
                );
                if (query.status() == InstallmentStatus.UNPAID) predicates.add(builder.greaterThanOrEqualTo(installment.get("dueDate"), LocalDate.now()));
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

            if (query.paymentType() != null) {
                predicates.add(
                        builder.equal(installment.get("expense").get("paymentType"), query.paymentType())
                );
            }

            if (query.categoryId() != null) {
                predicates.add(
                        builder.equal(installment.get("expense").get("category").get("id"), query.categoryId())
                );
            }

            criteria.orderBy(builder.asc(installment.get("dueDate")));

            criteria.where(predicates.toArray(new Predicate[0]));

            return em.createQuery(criteria).getResultList();
    }
}

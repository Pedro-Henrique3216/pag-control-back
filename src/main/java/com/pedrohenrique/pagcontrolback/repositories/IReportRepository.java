package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.dtos.response.MonthSummaryDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IReportRepository {

    List<MonthSummaryDto> findMonthlySummaryByUserId(UUID userId, LocalDate startDate, LocalDate endDate);
}

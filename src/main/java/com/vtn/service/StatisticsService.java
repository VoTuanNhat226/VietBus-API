package com.vtn.service;

import com.vtn.dto.request.StatisticsRequest;
import com.vtn.dto.response.StatisticsResponse;
import com.vtn.enumdef.PaymentStatusEnum;
import com.vtn.repository.PaymentRepository;
import com.vtn.repository.TicketRepository;
import com.vtn.repository.TripRepository;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
public class StatisticsService {
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;

    @Autowired
    public StatisticsService(
            PaymentRepository paymentRepository,
            TicketRepository ticketRepository,
            TripRepository tripRepository) {
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
        this.tripRepository = tripRepository;
    }

    public BaseResponse getRevenueByMonth(StatisticsRequest request) {
            // Parse month, format 2026-04
            YearMonth yearMonth = YearMonth.parse(request.getMonth());

            // ===== Current month =====
            LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

            // ===== Previous month =====
            YearMonth prevMonth = yearMonth.minusMonths(1);
            LocalDateTime prevStart = prevMonth.atDay(1).atStartOfDay();
            LocalDateTime prevEnd = yearMonth.atDay(1).atStartOfDay();

            // ===== Query =====
            BigDecimal revenue = paymentRepository.getRevenueByMonth(
                    PaymentStatusEnum.SUCCESS,
                    start,
                    end
            );

            BigDecimal revenuePrev = paymentRepository.getRevenueByMonth(
                    PaymentStatusEnum.SUCCESS,
                    prevStart,
                    prevEnd
            );

            // Null -> 0
            if (revenue == null) revenue = BigDecimal.ZERO;
            if (revenuePrev == null) revenuePrev = BigDecimal.ZERO;

            StatisticsResponse response = new StatisticsResponse();
            response.setRevenue(revenue);
            response.setRevenuePrev(revenuePrev);

            // (optional) % tăng trưởng
            if (revenuePrev.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal growth = revenue.subtract(revenuePrev)
                        .divide(revenuePrev, 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                response.setGrowthPercent(growth);
            } else {
                response.setGrowthPercent(BigDecimal.valueOf(100));
            }

            return new BaseResponse(200, response, "Get revenue by month successful", null, null);
    }

    public BaseResponse countTicketByMonth(StatisticsRequest request) {
        // Parse month, format 2026-04
        YearMonth yearMonth = YearMonth.parse(request.getMonth());

        // ===== Current month =====
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        // ===== Previous month =====
        YearMonth prevMonth = yearMonth.minusMonths(1);
        LocalDateTime prevStart = prevMonth.atDay(1).atStartOfDay();
        LocalDateTime prevEnd = yearMonth.atDay(1).atStartOfDay();

        // ===== Query =====
        BigDecimal total = ticketRepository.countTicketByMonth(
                start,
                end
        );

        BigDecimal totalPrev = ticketRepository.countTicketByMonth(
                prevStart,
                prevEnd
        );

        // Null -> 0
        if (total == null) total = BigDecimal.ZERO;
        if (totalPrev == null) totalPrev = BigDecimal.ZERO;

        StatisticsResponse response = new StatisticsResponse();
        response.setTotalTicket(total);
        response.setTotalTicketPrev(totalPrev);

        // (optional) % tăng trưởng
        if (totalPrev.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal growth = total.subtract(totalPrev)
                    .divide(totalPrev, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            response.setGrowthTicketPercent(growth);
        } else {
            response.setGrowthTicketPercent(BigDecimal.valueOf(100));
        }

        return new BaseResponse(200, response, "Get total ticket by month successful", null, null);
    }

    public BaseResponse countTripByMonth(StatisticsRequest request) {
        // Parse month, format 2026-04
        YearMonth yearMonth = YearMonth.parse(request.getMonth());

        // ===== Current month =====
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        // ===== Previous month =====
        YearMonth prevMonth = yearMonth.minusMonths(1);
        LocalDateTime prevStart = prevMonth.atDay(1).atStartOfDay();
        LocalDateTime prevEnd = yearMonth.atDay(1).atStartOfDay();

        // ===== Query =====
        BigDecimal total = tripRepository.countTripByMonth(
                start,
                end
        );

        BigDecimal totalPrev = tripRepository.countTripByMonth(
                prevStart,
                prevEnd
        );

        // Null -> 0
        if (total == null) total = BigDecimal.ZERO;
        if (totalPrev == null) totalPrev = BigDecimal.ZERO;

        StatisticsResponse response = new StatisticsResponse();
        response.setTotalTrip(total);
        response.setTotalTripPrev(totalPrev);

        // (optional) % tăng trưởng
        if (totalPrev.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal growth = total.subtract(totalPrev)
                    .divide(totalPrev, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            response.setGrowthTripPercent(growth);
        } else {
            response.setGrowthTripPercent(BigDecimal.valueOf(100));
        }

        return new BaseResponse(200, response, "Get total trip by month successful", null, null);
    }
}

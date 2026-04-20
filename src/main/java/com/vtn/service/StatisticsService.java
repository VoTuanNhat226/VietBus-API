package com.vtn.service;

import com.vtn.dto.request.StatisticsRequest;
import com.vtn.dto.response.StatisticsResponse;
import com.vtn.dto.response.TripResponse;
import com.vtn.entity.EmployeeEntity;
import com.vtn.entity.TripEntity;
import com.vtn.enumdef.PaymentStatusEnum;
import com.vtn.enumdef.TripStatusEnum;
import com.vtn.repository.*;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

@Service
public class StatisticsService {
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final PassengerRepository passengerRepository;
    private final VehicleRepository vehicleRepository;

    @Autowired
    public StatisticsService(
            PaymentRepository paymentRepository,
            TicketRepository ticketRepository,
            TripRepository tripRepository,
            PassengerRepository passengerRepository,
            VehicleRepository vehicleRepository) {
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
        this.tripRepository = tripRepository;
        this.passengerRepository = passengerRepository;
        this.vehicleRepository = vehicleRepository;
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

    public BaseResponse countPassengerByMonth(StatisticsRequest request) {
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
        BigDecimal total = passengerRepository.countPassengerByMonth(
                start,
                end
        );

        BigDecimal totalPrev = passengerRepository.countPassengerByMonth(
                prevStart,
                prevEnd
        );

        // Null -> 0
        if (total == null) total = BigDecimal.ZERO;
        if (totalPrev == null) totalPrev = BigDecimal.ZERO;

        StatisticsResponse response = new StatisticsResponse();
        response.setTotalPassenger(total);
        response.setTotalPassengerPrev(totalPrev);

        // (optional) % tăng trưởng
        if (totalPrev.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal growth = total.subtract(totalPrev)
                    .divide(totalPrev, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            response.setGrowthPassengerPercent(growth);
        } else {
            response.setGrowthPassengerPercent(BigDecimal.valueOf(100));
        }

        return new BaseResponse(200, response, "Get total passenger by month successful", null, null);
    }

    public BaseResponse getTotalVehicle(StatisticsRequest request) {
        // ===== Query =====
        BigDecimal active = vehicleRepository.countVehiclesActiveByStatus(true);
        BigDecimal inActive = vehicleRepository.countVehiclesActiveByStatus(false);

        // Null -> 0
        if (active == null) active = BigDecimal.ZERO;
        if (inActive == null) inActive = BigDecimal.ZERO;

        StatisticsResponse response = new StatisticsResponse();
        response.setTotalVehicleActive(active);
        response.setTotalVehicleInActive(inActive);

        return new BaseResponse(200, response, "Get all vehicle successful", null, null);
    }

    public BaseResponse getAllTripDeparted(StatisticsRequest request) {
        List<TripEntity> trips = tripRepository.getAllByStatus(TripStatusEnum.DEPARTED);
        if (trips != null && !trips.isEmpty()) {
            List<TripResponse> responses = trips.stream()
                    .map(this::TriptoResponse)
                    .toList();

            return new BaseResponse(200, responses, "Get all trip departed successful", null, null);
        }
        return new BaseResponse(200, Collections.emptyList(), "Get all trip departed successful", null, null);
    }

    // Helper
    private TripResponse TriptoResponse(TripEntity trip) {
        return TripResponse.builder()
                .tripId(trip.getTripId())
                .tripCode(trip.getTripCode())
                .departureTime(trip.getDepartureTime())
                .arrivalTime(trip.getArrivalTime())
                .price(trip.getPrice())
                .status(trip.getStatus())
                .fromStation(trip.getRoute().getFromStation().getName())
                .toStation(trip.getRoute().getToStation().getName())
                .licensePlate(trip.getVehicle().getLicensePlate())
                .totalSeat(trip.getVehicle().getTotalSeat())
                .driverNames(trip.getDrivers().stream()
                        .map(EmployeeEntity::getFullName).toList())
                .assistantNames(trip.getAssistants().stream()
                        .map(EmployeeEntity::getFullName).toList())
                .build();
    }
}

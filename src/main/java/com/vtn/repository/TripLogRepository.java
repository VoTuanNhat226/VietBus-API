package com.vtn.repository;

import com.vtn.entity.TripLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripLogRepository extends JpaRepository<TripLog, Long> {
}

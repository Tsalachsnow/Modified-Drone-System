package com.musalasoft.services.repositories;

import com.musalasoft.models.DispatchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DispatchLogRepository extends JpaRepository<DispatchLog, Long> {
   Optional<DispatchLog> findByDispatchNumber(String number);
//   Optional<DispatchLog> findByDispatchNumber1(String number);
}

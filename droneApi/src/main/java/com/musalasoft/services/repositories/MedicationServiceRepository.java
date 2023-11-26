package com.musalasoft.services.repositories;

import com.musalasoft.models.Drone;
import com.musalasoft.models.Medication;
import com.musalasoft.enums.State;
import org.hibernate.Hibernate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MedicationServiceRepository extends JpaRepository<Medication, Long> {
     Medication findByDrone_SerialNumber(String serialNumber);
    List<Medication> findAllByDrone_SerialNumberAndStatus(String serialNumber, State status);
}
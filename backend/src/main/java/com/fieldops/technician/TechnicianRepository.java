package com.fieldops.technician;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {
    List<Technician> findByStatus(TechnicianStatus status);
    List<Technician> findByCity(String city);
}

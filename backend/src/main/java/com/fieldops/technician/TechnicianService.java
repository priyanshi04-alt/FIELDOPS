package com.fieldops.technician;

import com.fieldops.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TechnicianService {

    private final TechnicianRepository technicianRepository;

    public TechnicianService(TechnicianRepository technicianRepository) {
        this.technicianRepository = technicianRepository;
    }

    @Transactional(readOnly = true)
    public List<TechnicianDTO> getAllTechnicians(TechnicianStatus status, String city) {
        List<Technician> list;
        if (status != null) {
            list = technicianRepository.findByStatus(status);
        } else if (city != null && !city.isBlank()) {
            list = technicianRepository.findByCity(city);
        } else {
            list = technicianRepository.findAll();
        }
        return list.stream().map(TechnicianDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TechnicianDTO getTechnicianById(Long id) {
        Technician t = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + id));
        return TechnicianDTO.fromEntity(t);
    }

    @Transactional
    public TechnicianDTO updateStatus(Long id, TechnicianStatus status) {
        Technician t = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + id));
        t.setStatus(status);
        return TechnicianDTO.fromEntity(technicianRepository.save(t));
    }
}

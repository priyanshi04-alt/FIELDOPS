package com.fieldops.predictive;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModelRegistryRepository extends JpaRepository<ModelRegistryEntry, Long> {
    Optional<ModelRegistryEntry> findByModelKey(String modelKey);
}

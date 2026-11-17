package com.fieldops.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByCity(String city);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Customer c SET c.openJobsCount = c.openJobsCount + 1 WHERE c.id = :customerId")
    int incrementOpenJobsCount(@Param("customerId") Long customerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Customer c SET c.openJobsCount = CASE WHEN c.openJobsCount > 0 THEN c.openJobsCount - 1 ELSE 0 END, c.completedJobsCount = c.completedJobsCount + 1 WHERE c.id = :customerId")
    int transitionJobCompleted(@Param("customerId") Long customerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Customer c SET c.openJobsCount = CASE WHEN c.openJobsCount > 0 THEN c.openJobsCount - 1 ELSE 0 END WHERE c.id = :customerId")
    int decrementOpenJobsCount(@Param("customerId") Long customerId);
}

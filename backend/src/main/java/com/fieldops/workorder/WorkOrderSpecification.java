package com.fieldops.workorder;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class WorkOrderSpecification {

    public static Specification<WorkOrder> filterBy(
            WorkOrderStatus status,
            WorkOrderPriority priority,
            Long technicianId,
            Long customerId,
            String search,
            String customerEmail,
            String technicianEmail
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            if (technicianId != null) {
                predicates.add(cb.equal(root.get("technician").get("id"), technicianId));
            }

            if (customerId != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            }

            // Customer IDOR isolation at SQL level
            if (customerEmail != null && !customerEmail.isBlank()) {
                String email = customerEmail.trim().toLowerCase();
                String localPart = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
                predicates.add(cb.or(
                        cb.equal(cb.lower(root.get("customer").get("email")), email),
                        cb.like(cb.lower(root.get("customer").get("email")), localPart + "%")
                ));
            }

            // Technician IDOR isolation at SQL level
            if (technicianEmail != null && !technicianEmail.isBlank()) {
                String email = technicianEmail.trim().toLowerCase();
                String localPart = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
                predicates.add(cb.or(
                        cb.equal(cb.lower(root.get("technician").get("email")), email),
                        cb.like(cb.lower(root.get("technician").get("email")), localPart + "%")
                ));
            }

            // Keyword search across work order number, title, and customer company/name
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                Predicate woNumMatch = cb.like(cb.lower(root.get("workOrderNumber")), term);
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), term);
                Predicate custNameMatch = cb.like(cb.lower(root.get("customer").get("name")), term);
                Predicate custCompanyMatch = cb.like(cb.lower(root.get("customer").get("company")), term);
                predicates.add(cb.or(woNumMatch, titleMatch, custNameMatch, custCompanyMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

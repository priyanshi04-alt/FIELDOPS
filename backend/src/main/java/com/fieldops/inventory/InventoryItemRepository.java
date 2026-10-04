package com.fieldops.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByCategory(String category);
    List<InventoryItem> findByStatus(InventoryItemStatus status);

    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.status = 'LOW_STOCK'")
    long countLowStock();

    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.status = 'OUT_OF_STOCK'")
    long countOutOfStock();
}

package com.fieldops.inventory;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String category;

    private Integer availableQuantity = 0;
    private Integer reservedQuantity = 0;
    private Integer minThreshold = 10;
    private Double unitPrice = 0.0;
    private String unitOfMeasure = "units";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryItemStatus status = InventoryItemStatus.IN_STOCK;

    private String warehouseLocation;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public InventoryItem() {}

    public InventoryItem(Long id, String name, String sku, String category, Integer availableQuantity,
                         Integer reservedQuantity, Integer minThreshold, Double unitPrice,
                         String unitOfMeasure, InventoryItemStatus status, String warehouseLocation) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.availableQuantity = availableQuantity != null ? availableQuantity : 0;
        this.reservedQuantity = reservedQuantity != null ? reservedQuantity : 0;
        this.minThreshold = minThreshold != null ? minThreshold : 10;
        this.unitPrice = unitPrice != null ? unitPrice : 0.0;
        this.unitOfMeasure = unitOfMeasure != null ? unitOfMeasure : "units";
        this.status = status != null ? status : InventoryItemStatus.IN_STOCK;
        this.warehouseLocation = warehouseLocation;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private String sku;
        private String category;
        private Integer availableQuantity = 0;
        private Integer reservedQuantity = 0;
        private Integer minThreshold = 10;
        private Double unitPrice = 0.0;
        private String unitOfMeasure = "units";
        private InventoryItemStatus status = InventoryItemStatus.IN_STOCK;
        private String warehouseLocation;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder sku(String sku) { this.sku = sku; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder availableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; return this; }
        public Builder reservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; return this; }
        public Builder minThreshold(Integer minThreshold) { this.minThreshold = minThreshold; return this; }
        public Builder unitPrice(Double unitPrice) { this.unitPrice = unitPrice; return this; }
        public Builder unitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; return this; }
        public Builder status(InventoryItemStatus status) { this.status = status; return this; }
        public Builder warehouseLocation(String warehouseLocation) { this.warehouseLocation = warehouseLocation; return this; }

        public InventoryItem build() {
            return new InventoryItem(id, name, sku, category, availableQuantity, reservedQuantity,
                    minThreshold, unitPrice, unitOfMeasure, status, warehouseLocation);
        }
    }

    public void updateStatus() {
        if (availableQuantity <= 0) {
            this.status = InventoryItemStatus.OUT_OF_STOCK;
        } else if (availableQuantity <= minThreshold) {
            this.status = InventoryItemStatus.LOW_STOCK;
        } else {
            this.status = InventoryItemStatus.IN_STOCK;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    public Integer getMinThreshold() { return minThreshold; }
    public void setMinThreshold(Integer minThreshold) { this.minThreshold = minThreshold; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public InventoryItemStatus getStatus() { return status; }
    public void setStatus(InventoryItemStatus status) { this.status = status; }
    public String getWarehouseLocation() { return warehouseLocation; }
    public void setWarehouseLocation(String warehouseLocation) { this.warehouseLocation = warehouseLocation; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

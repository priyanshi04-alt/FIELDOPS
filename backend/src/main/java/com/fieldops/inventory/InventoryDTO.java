package com.fieldops.inventory;

import java.time.LocalDateTime;

public class InventoryDTO {
    private Long id;
    private String name;
    private String sku;
    private String category;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer minThreshold;
    private Double unitPrice;
    private String unitOfMeasure;
    private InventoryItemStatus status;
    private String warehouseLocation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InventoryDTO() {}

    public InventoryDTO(Long id, String name, String sku, String category, Integer availableQuantity,
                        Integer reservedQuantity, Integer minThreshold, Double unitPrice,
                        String unitOfMeasure, InventoryItemStatus status, String warehouseLocation,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
        this.minThreshold = minThreshold;
        this.unitPrice = unitPrice;
        this.unitOfMeasure = unitOfMeasure;
        this.status = status;
        this.warehouseLocation = warehouseLocation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static InventoryDTO fromEntity(InventoryItem item) {
        if (item == null) return null;
        return new InventoryDTO(
                item.getId(),
                item.getName(),
                item.getSku(),
                item.getCategory(),
                item.getAvailableQuantity(),
                item.getReservedQuantity(),
                item.getMinThreshold(),
                item.getUnitPrice(),
                item.getUnitOfMeasure(),
                item.getStatus(),
                item.getWarehouseLocation(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
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

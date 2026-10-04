package com.fieldops.inventory;

import com.fieldops.common.BadRequestException;
import com.fieldops.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    public InventoryService(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @Transactional(readOnly = true)
    public List<InventoryDTO> getAllItems(String category, InventoryItemStatus status) {
        List<InventoryItem> list;
        if (category != null && !category.isBlank()) {
            list = inventoryItemRepository.findByCategory(category);
        } else if (status != null) {
            list = inventoryItemRepository.findByStatus(status);
        } else {
            list = inventoryItemRepository.findAll();
        }
        return list.stream().map(InventoryDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InventoryDTO getItemById(Long id) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + id));
        return InventoryDTO.fromEntity(item);
    }

    @Transactional
    public InventoryDTO adjustStock(Long id, StockAdjustRequest request) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + id));

        int newQuantity = item.getAvailableQuantity() + request.getAmount();
        if (newQuantity < 0) {
            throw new BadRequestException("Insufficient inventory available. Current: " + item.getAvailableQuantity());
        }

        item.setAvailableQuantity(newQuantity);
        item.updateStatus();

        InventoryItem saved = inventoryItemRepository.save(item);
        return InventoryDTO.fromEntity(saved);
    }
}

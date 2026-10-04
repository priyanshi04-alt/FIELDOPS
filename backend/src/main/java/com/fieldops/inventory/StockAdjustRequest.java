package com.fieldops.inventory;

import jakarta.validation.constraints.NotNull;

public class StockAdjustRequest {
    @NotNull(message = "Adjustment amount is required")
    private Integer amount;
    private String reason;

    public StockAdjustRequest() {}

    public StockAdjustRequest(Integer amount, String reason) {
        this.amount = amount;
        this.reason = reason;
    }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

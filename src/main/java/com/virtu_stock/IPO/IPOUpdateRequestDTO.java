package com.virtu_stock.IPO;

import java.util.Map;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IPOUpdateRequestDTO {
    @NotNull(message = "Subscriptions cannot be null")
    private Map<@NotEmpty(message = "Subscription name cannot be empty") String, @Min(value = 0, message = "Subscription value must be non-negative") Double> subscriptions;

    @NotNull(message = "Subscriptions cannot be null")
    private IssueSize issueSize;
}

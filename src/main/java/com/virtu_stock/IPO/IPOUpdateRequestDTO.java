package com.virtu_stock.IPO;

import java.util.List;
import java.util.Map;

import com.virtu_stock.Enum.Verdict;
import com.virtu_stock.GMP.GMP;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IPOUpdateRequestDTO {

    @NotNull
    private Map<String, Double> subscriptions;
    @NotNull
    private IssueSize issueSize;
    @NotNull
    private List<GMP> gmp;
    @NotNull
    private Verdict verdict;
    @NotNull
    private Double listedPrice;
}

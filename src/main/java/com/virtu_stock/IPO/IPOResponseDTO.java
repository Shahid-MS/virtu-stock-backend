package com.virtu_stock.IPO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.virtu_stock.Enum.IPOStatus;
import com.virtu_stock.Enum.Verdict;
import com.virtu_stock.GMP.GMP;

import lombok.Data;

@Data
public class IPOResponseDTO {

    private UUID id;
    private String name;
    private String symbol;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate listingDate;
    private Double minPrice;
    private Double listedPrice;
    private Double maxPrice;

    private Integer minQty;

    private String logo;

    private IssueSize issueSize;

    private String about;

    private LocalDate allotmentDate;

    private Verdict verdict;

    private IPOStatus status;

    private List<String> strengths;

    private List<String> risks;

    private Map<String, Double> subscriptions;

    private List<GMP> gmp;
    private Double listingReturn;
    private Double listingReturnPercent;
}

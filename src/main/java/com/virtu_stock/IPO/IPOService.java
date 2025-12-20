package com.virtu_stock.IPO;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.virtu_stock.Configurations.AppConstants;
import com.virtu_stock.Enum.IPOStatus;
import com.virtu_stock.Enum.Verdict;
import com.virtu_stock.Exceptions.CustomExceptions.BadRequestException;
import com.virtu_stock.Exceptions.CustomExceptions.InvalidPaginationParameterException;
import com.virtu_stock.Exceptions.CustomExceptions.InvalidSortFieldException;
import com.virtu_stock.Exceptions.CustomExceptions.ResourceNotFoundException;
import com.virtu_stock.GMP.GMP;
import com.virtu_stock.Pagination.PageResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IPOService {

    private final IPORepository ipoRepository;
    private final ModelMapper modelMapper;

    public PageResponseDTO<IPOResponseDTO> findAll(int pageNumber, int pageSize, String sortBy, String sortDir) {
        if (pageNumber < 0 || pageSize <= 0) {
            throw new InvalidPaginationParameterException(
                    "Page number and size must be positive");
        }

        if (pageSize > AppConstants.PAGE_SIZE_MAX_LIMIT) {
            throw new InvalidPaginationParameterException(
                    "Page size cannot exceed " + AppConstants.PAGE_SIZE_MAX_LIMIT);
        }

        List<String> allowedSortFields = List.of("startDate", "name");

        if (!allowedSortFields.contains(sortBy)) {
            throw new InvalidSortFieldException(
                    "Invalid sort field. Allowed values: " + allowedSortFields);
        }
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<IPO> pageDetails = ipoRepository.findAll(pageable);
        List<IPO> ipos = pageDetails.getContent();
        List<IPOResponseDTO> iposDTO = ipos.stream().map(ipo -> modelMapper.map(ipo, IPOResponseDTO.class)).toList();
        PageResponseDTO<IPOResponseDTO> ipoPageResponseDTO = new PageResponseDTO<IPOResponseDTO>();
        ipoPageResponseDTO.setContent(iposDTO);
        ipoPageResponseDTO.setPageNumber(pageDetails.getNumber());
        ipoPageResponseDTO.setPageSize(pageDetails.getSize());
        ipoPageResponseDTO.setTotalPageElements(pageDetails.getNumberOfElements());
        ipoPageResponseDTO.setTotalPages(pageDetails.getTotalPages());
        ipoPageResponseDTO.setTotalElements(pageDetails.getTotalElements());
        ipoPageResponseDTO.setLastPage(pageDetails.isLast());
        return ipoPageResponseDTO;
    }

    public List<IPO> findByStatus(String status) {

        try {
            IPOStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid IPO status: " + status);
        }
        return ipoRepository.findAll().stream()
                .filter(ipo -> ipo.getStatus() == IPOStatus.valueOf(status.toUpperCase()))
                .toList();

    }

    public IPO findById(UUID id) {
        return ipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IPO", "id", id));
    }

    public IPO save(IPO ipo) {
        return ipoRepository.save(ipo);
    }

    public IPOResponseDTO updateIpo(UUID id, IPOUpdateRequestDTO ipoReq) {
        IPO ipo = findById(id);
        if (ipoReq.getSubscriptions() != null) {
            Set<String> PROTECTED_KEYS = Set.of(
                    "QIB", "Non-Institutional", "Retailer", "Total");
            for (String protectedKey : PROTECTED_KEYS) {
                if (!ipoReq.getSubscriptions().containsKey(protectedKey)) {
                    throw new BadRequestException(
                            "Subscription '" + protectedKey + "' cannot be deleted");
                }
            }

            ipo.getSubscriptions().keySet().removeIf(key -> !PROTECTED_KEYS.contains(key) &&
                    !ipoReq.getSubscriptions().containsKey(key));
        }

        modelMapper.map(ipoReq, ipo);
        IPO savedIpo = save(ipo);
        return modelMapper.map(savedIpo, IPOResponseDTO.class);
    }

    public void updateGmp(IPO existingIpo, List<GMP> newGmp) {
        List<GMP> existingGMP = existingIpo.getGmp();
        for (GMP g : newGmp) {
            Optional<GMP> foundGMP = existingGMP.stream().filter(s -> s.getGmpDate().equals(g.getGmpDate()))
                    .findFirst();
            if (foundGMP.isPresent()) {
                if (foundGMP.get().getGmp() != g.getGmp()) {
                    foundGMP.get().setGmp(g.getGmp());
                    foundGMP.get().setLastUpdated(LocalDateTime.now());
                }
            } else {
                existingGMP.add(GMP.builder().gmp(g.getGmp()).gmpDate(g.getGmpDate())
                        .lastUpdated(LocalDateTime.now()).build());
            }
        }
        existingIpo.setGmp(existingGMP);
    }

    public void updateVerdict(IPO existingIpo, Verdict newVerdict) {
        if (existingIpo.getVerdict() != newVerdict) {
            existingIpo.setVerdict(newVerdict);
        }
    }

    public void updateIssueSize(IPO existingIpo, IssueSize newIssueSize) {
        if (existingIpo.getIssueSize().getFresh() != newIssueSize.getFresh()) {
            existingIpo.getIssueSize().setFresh(newIssueSize.getFresh());
        }
        if (existingIpo.getIssueSize().getOfferForSale() != newIssueSize.getOfferForSale()) {
            existingIpo.getIssueSize().setOfferForSale(newIssueSize.getOfferForSale());
        }
        if (existingIpo.getIssueSize().getTotalIssueSize() != newIssueSize.getTotalIssueSize()) {
            existingIpo.getIssueSize().setTotalIssueSize(newIssueSize.getTotalIssueSize());
        }
    }

    public void deleteById(UUID id) {
        ipoRepository.deleteById(id);
    }

    public List<IPO> fetchIPOByListingPending() {
        List<IPO> ipos = ipoRepository.findByListingDateLessThanEqual(LocalDate.now());
        return ipos;
    }

    public List<Object[]> getIpoCountByMonthAndYear(Integer year) {
        return ipoRepository.countIpoByMonthAndYear(year);
    }

    public List<IPO> search(String query) {
        query = query.trim();
        if (query.length() <= 2) {
            return List.of();
        }
        List<IPO> search = ipoRepository.findByNameContainingIgnoreCaseOrSymbolContainingIgnoreCaseOrderByName(query,
                query);
        return search.stream().limit(7).toList();
    }

}

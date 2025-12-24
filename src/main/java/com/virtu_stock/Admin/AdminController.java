package com.virtu_stock.Admin;

import java.util.HashMap;

import java.util.Map;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.virtu_stock.IPO.IPOResponseDTO;
import com.virtu_stock.IPO.IPOService;
import com.virtu_stock.IPO.IPOUpdateRequestDTO;

import com.virtu_stock.Security.Util.AuthUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IPOService ipoService;

    private final AsyncService asyncService;

    @GetMapping("/ipo/fetch")
    public ResponseEntity<Map<String, Object>> fetchIPO(
            @RequestParam(required = false, defaultValue = "open") String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int limit) {
        String email = AuthUtil.getCurrentUserEmail();

        asyncService.fetchIPOInBackground(status, type, limit, email);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "IPO fetch process started");
        res.put("note", "You will receive an email once the task is completed");

        return ResponseEntity.ok(res);
    }

    @PutMapping("/ipo/{id}")
    public ResponseEntity<?> updateIpo(@PathVariable UUID id, @RequestBody IPOUpdateRequestDTO ipoReq) {

        IPOResponseDTO ipo = ipoService.updateIpo(id, ipoReq);
        return ResponseEntity.ok(Map.of("message", "Updated Successfully", "IPO", ipo));

        // ObjectMapper mapper = new ObjectMapper();
        // mapper.registerModule(new JavaTimeModule());
        // mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // ipoNode.fieldNames().forEachRemaining(fieldName -> {
        // switch (fieldName.toLowerCase()) {
        // // case "subscriptions" -> {
        // // List<Subscription> newSubs = Arrays.asList(
        // // mapper.convertValue(ipoNode.get(fieldName), Subscription[].class));
        // // ipoService.updateSubscriptions(existingIpo, newSubs);
        // // }

        // case "gmp" -> {
        // List<GMP> newGmp = Arrays.asList(
        // mapper.convertValue(ipoNode.get(fieldName), GMP[].class));
        // ipoService.updateGmp(existingIpo,
        // newGmp);
        // }

        // case "verdict" -> {
        // String verdictStr = ipoNode.get(fieldName).asText();
        // try {
        // Verdict newVerdict = Verdict.valueOf(verdictStr.toUpperCase());
        // ipoService.updateVerdict(existingIpo, newVerdict);
        // } catch (IllegalArgumentException e) {
        // throw new BadRequestException("Invalid Verdict Value: " + verdictStr);
        // }
        // }

        // case "issuesize" -> {
        // IssueSize newIssueSize = mapper.convertValue(ipoNode.get(fieldName),
        // IssueSize.class);
        // ipoService.updateIssueSize(existingIpo, newIssueSize);
        // }

        // default -> throw new BadRequestException("Invalid field: " + fieldName);

        // }
        // });

        // IPO updatedIpo = ipoService.save(existingIpo);
        // return ResponseEntity.ok(updatedIpo);

    }

    @DeleteMapping("/ipo/{id}")
    public ResponseEntity<?> DeleteIpo(@PathVariable UUID id) {
        ipoService.findById(id);
        ipoService.deleteById(id);
        return ResponseEntity.ok().body("IPO deleted with id: " + id);
    }
}

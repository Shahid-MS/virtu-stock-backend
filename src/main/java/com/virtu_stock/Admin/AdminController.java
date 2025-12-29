package com.virtu_stock.Admin;

import java.util.HashMap;

import java.util.Map;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/ipo/fetch")
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
    }

    @DeleteMapping("/ipo/{id}")
    public ResponseEntity<?> DeleteIpo(@PathVariable UUID id) {
        ipoService.findById(id);
        ipoService.deleteById(id);
        return ResponseEntity.ok().body("IPO deleted with id: " + id);
    }
}

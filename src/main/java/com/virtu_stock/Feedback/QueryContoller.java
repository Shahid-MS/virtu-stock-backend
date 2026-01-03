package com.virtu_stock.Feedback;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.virtu_stock.Mail.MailService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feedback/query")
@RequiredArgsConstructor
public class QueryContoller {
    private final MailService mailService;

    @PostMapping
    public ResponseEntity<?> submitQuery(Principal principal, @RequestBody @Valid QueryRequestDTO req) {
        String email;
        if (principal != null) {
            email = principal.getName();
        } else {
            if (req.getEmail() == null || req.getEmail().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Email is required for guest Users"));
            }
            email = req.getEmail();
        }
        mailService.sendQueryEmail(email, req.getMessage());
        return ResponseEntity.ok(Map.of("message", "Your Query is Submitted. We will get back to you Soon"));
    }
}

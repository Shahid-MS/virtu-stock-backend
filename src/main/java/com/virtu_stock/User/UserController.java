package com.virtu_stock.User;

import java.security.Principal;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;

    @GetMapping()
    public ResponseEntity<?> userDetails(Principal principal) {
        String email = principal.getName();
        User user = userService.findByEmail(email);
        UserResponseDTO userRes = modelMapper.map(user, UserResponseDTO.class);
        return ResponseEntity.ok(userRes);
    }

    @PatchMapping()
    public ResponseEntity<?> updateUser(Principal principal, @RequestBody Map<String, Object> updates) {
        String email = principal.getName();
        userService.updateUser(email, updates);
        return ResponseEntity.ok(Map.of("message", "User updated Successfully"));
    }

}

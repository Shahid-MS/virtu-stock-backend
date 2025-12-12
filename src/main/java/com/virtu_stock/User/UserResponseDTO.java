package com.virtu_stock.User;

import java.util.List;
import java.util.UUID;

import com.virtu_stock.Enum.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private List<Role> roles;
    private String phone;
    private String instagramUrl;
    private String linkedinUrl;
}

package com.virtu_stock.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtu_stock.Enum.Role;
import com.virtu_stock.Exceptions.CustomExceptions.BadRequestException;
import com.virtu_stock.Exceptions.CustomExceptions.DuplicateResourceException;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final ModelMapper modelMapper;

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(List.of(Role.ROLE_USER));
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);

    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User findByEmail(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        return optionalUser.orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User setPassword(String email, String password) {
        User user = findByEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public UserResponseDTO updateUser(User user, Map<String, Object> updates) {
        UserRequestDTO dto = new ObjectMapper().convertValue(updates, UserRequestDTO.class);
        Set<ConstraintViolation<UserRequestDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            List<Map<String, String>> errors = violations.stream()
                    .map(v -> Map.of(
                            "field", v.getPropertyPath().toString(),
                            "message", v.getMessage()))
                    .toList();
            String errorMessage;
            try {
                errorMessage = new ObjectMapper().writeValueAsString(errors);
            } catch (JsonProcessingException e) {
                errorMessage = "Validation failed";
            }
            throw new BadRequestException(errorMessage.toString());
        }

        ModelMapper localMapper = new ModelMapper();
        localMapper.getConfiguration().setSkipNullEnabled(true);
        localMapper.map(dto, user);
        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserResponseDTO.class);
    }
}

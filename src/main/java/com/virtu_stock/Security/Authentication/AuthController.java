package com.virtu_stock.Security.Authentication;

import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.virtu_stock.Exceptions.CustomExceptions.BadRequestException;
import com.virtu_stock.Exceptions.CustomExceptions.DuplicateResourceException;
import com.virtu_stock.Exceptions.CustomExceptions.ResourceNotFoundException;
import com.virtu_stock.Exceptions.CustomExceptions.UnauthorizedException;
// import com.virtu_stock.Mail.MailBoxLayer.EmailVerificationService;
import com.virtu_stock.Mail.MailService;
import com.virtu_stock.Mail.OTP.OTPPurpose;
import com.virtu_stock.Mail.OTP.OTPService;
import com.virtu_stock.Security.Authentication.DTO.AuthRequestDTO;
import com.virtu_stock.Security.Authentication.DTO.VerifyOTPRequestDTO;
import com.virtu_stock.Security.JWT.JWTUtil;
import com.virtu_stock.User.CustomUserDetails;
import com.virtu_stock.User.CustomUserDetailsService;
import com.virtu_stock.User.User;
import com.virtu_stock.User.UserResponseDTO;
import com.virtu_stock.User.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JWTUtil jwtUtil;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final OTPService otpService;
    // private final EmailVerificationService emailVerificationService;
    private final MailService mailService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDTO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        var userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

        String jwt = jwtUtil.generateToken(customUserDetails);

        return ResponseEntity.ok(Map.of("message", "Login Successfully", "virtustock-token", jwt));

    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestHeader(value = "x-otp-verify-token", required = false) String token,
            @Valid @RequestBody User user) {

        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Invalid request. Please verify your email first.");
        }

        String emailFromToken = jwtUtil.validateOTPToken(token, OTPPurpose.SIGN_UP);
        if (emailFromToken == null || !emailFromToken.equals(user.getEmail())) {
            throw new UnauthorizedException("Invalid email");
        }

        if (!otpService.isEmailVerified(user.getEmail(), OTPPurpose.SIGN_UP)) {
            throw new BadRequestException("Please verify your email before registration.");
        }

        User savedUser = userService.registerUser(user);
        otpService.deleteByEmailAndPurpose(user.getEmail(), OTPPurpose.SIGN_UP);
        mailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName());

        UserResponseDTO res = modelMapper.map(savedUser, UserResponseDTO.class);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "User Created Successfully",
                        "user", res));
    }

    @PostMapping("/register/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody EmailRequestDTO request) {
        String email = request.getEmail();

        if (!mailService.hasMXRecord(email)) {
            throw new BadRequestException("Invalid email domain. Please enter a valid email address.");
        }

        if (userService.existsByEmail(email)) {
            throw new DuplicateResourceException("User Already exists");
        }

        // if (!emailVerificationService.verifyEmail(email)) {
        // return ResponseEntity.badRequest()
        // .body(Map.of("message", "Invalid email. Please enter a valid email
        // address."));
        // }

        otpService.generateAndSendOtp(email, OTPPurpose.SIGN_UP);
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully to " + email + ". Valid for 5 minutes"));

    }

    @PostMapping("/register/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOTPRequestDTO request) {
        String email = request.getEmail();
        String otp = request.getOtp();

        boolean verified = otpService.verifyOtp(email, otp, OTPPurpose.SIGN_UP);

        if (!verified) {
            throw new BadRequestException("Invalid Email or expired OTP");
        }

        String token = jwtUtil.generateOTPToken(email, OTPPurpose.SIGN_UP);

        return ResponseEntity.ok(
                Map.of(
                        "message", "OTP verified successfully",
                        "otpToken", token));

    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody EmailRequestDTO request) {
        String email = request.getEmail();

        if (!userService.existsByEmail(email)) {
            throw new ResourceNotFoundException("User", "email", email);
        }
        otpService.generateAndSendOtp(email, OTPPurpose.FORGOT_PASSWORD);
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully to " + email + ". Valid for 5 minutes."));

    }

    @PostMapping("/verify-forgot-otp")
    public ResponseEntity<?> verifyForgotOtp(@Valid @RequestBody VerifyOTPRequestDTO request) {

        String email = request.getEmail();
        String otp = request.getOtp();

        boolean verified = otpService.verifyOtp(email, otp, OTPPurpose.FORGOT_PASSWORD);

        if (!verified) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        String token = jwtUtil.generateOTPToken(email, OTPPurpose.FORGOT_PASSWORD);

        return ResponseEntity.ok(
                Map.of(
                        "message", "OTP verified successfully",
                        "otpToken", token));

    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestHeader(value = "x-otp-verify-token", required = false) String token,
            @Valid @RequestBody AuthRequestDTO request) {

        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Invalid request. Please verify your email first.");
        }

        String email = request.getEmail();
        String password = request.getPassword();

        String emailFromToken = jwtUtil.validateOTPToken(token, OTPPurpose.FORGOT_PASSWORD);

        if (emailFromToken == null || !emailFromToken.equals(email)) {
            throw new UnauthorizedException("Invalid Email");
        }
        if (!otpService.isEmailVerified(email, OTPPurpose.FORGOT_PASSWORD)) {
            throw new BadRequestException("Please verify your email before resetting password.");
        }

        User savedUser = userService.setPassword(email, password);
        otpService.deleteByEmailAndPurpose(savedUser.getEmail(), OTPPurpose.FORGOT_PASSWORD);
        mailService.sendPasswordResetMail(savedUser.getEmail(), savedUser.getFirstName());
        return ResponseEntity.ok(Map.of("message", "Password Succesfully updated"));

    }

}

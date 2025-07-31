package com.lexiai.controller;

import com.lexiai.dto.AuthResponse;
import com.lexiai.dto.LoginRequest;
import com.lexiai.dto.RegisterRequest;
import com.lexiai.service.AuthService;
import com.lexiai.model.Lawyer;
import com.lexiai.repository.LawyerRepository;
import com.lexiai.security.UserPrincipal;
import com.lexiai.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Authentication", description = "User authentication and registration APIs")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private LawyerRepository lawyerRepository;
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "400", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse("Login failed: " + e.getMessage(), false));
        }
    }
    
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register new lawyer with firm details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration successful"),
        @ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            AuthResponse response = authService.register(registerRequest);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse("Registration failed: " + e.getMessage(), false));
        }
    }
    
    @GetMapping("/check-email")
    @Operation(summary = "Check email availability", description = "Check if email is available for registration")
    @ApiResponse(responseCode = "200", description = "Email availability status")
    public ResponseEntity<Boolean> checkEmailAvailability(@RequestParam String email) {
        boolean isAvailable = authService.isEmailAvailable(email);
        return ResponseEntity.ok(isAvailable);
    }
    
    @GetMapping("/me")
    @PreAuthorize("hasRole('LAWYER')")
    @Operation(summary = "Get current user", description = "Get current authenticated user details")
    @ApiResponse(responseCode = "200", description = "User details retrieved successfully")
    public ResponseEntity<Lawyer> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        Optional<Lawyer> lawyer = lawyerRepository.findByEmail(userPrincipal.getEmail());
        if (lawyer.isPresent()) {
            return ResponseEntity.ok(lawyer.get());
        } else {
            throw new ResourceNotFoundException("Lawyer", "email", userPrincipal.getEmail());
        }
    }
    
    @PostMapping("/logout")
    @PreAuthorize("hasRole('LAWYER')")
    @Operation(summary = "User logout", description = "Logout current user")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}

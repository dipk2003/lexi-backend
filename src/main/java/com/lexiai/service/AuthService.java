package com.lexiai.service;

import com.lexiai.dto.AuthResponse;
import com.lexiai.dto.LoginRequest;
import com.lexiai.dto.RegisterRequest;
import com.lexiai.exception.AuthenticationException;
import com.lexiai.model.Firm;
import com.lexiai.model.Lawyer;
import com.lexiai.repository.FirmRepository;
import com.lexiai.repository.LawyerRepository;
import com.lexiai.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private LawyerRepository lawyerRepository;
    
    @Autowired
    private FirmRepository firmRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(), 
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            Lawyer lawyer = lawyerRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AuthenticationException("User not found"));
            
            // Update last login
            lawyer.setLastLogin(LocalDateTime.now());
            lawyerRepository.save(lawyer);
            
            return new AuthResponse(jwt, lawyer);
            
        } catch (Exception e) {
            throw new AuthenticationException("Invalid email or password");
        }
    }
    
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if email already exists
        if (lawyerRepository.existsByEmail(registerRequest.getEmail())) {
            return new AuthResponse("Email is already in use!", false);
        }
        
        // Create or find firm
        Firm firm = firmRepository.findByName(registerRequest.getFirmName())
            .orElseGet(() -> {
                Firm newFirm = new Firm();
                newFirm.setName(registerRequest.getFirmName());
                newFirm.setEmail(registerRequest.getFirmEmail());
                newFirm.setPhoneNumber(registerRequest.getFirmPhone());
                newFirm.setAddress(registerRequest.getFirmAddress());
                newFirm.setCity(registerRequest.getFirmCity());
                newFirm.setState(registerRequest.getFirmState());
                newFirm.setCountry(registerRequest.getFirmCountry());
                return firmRepository.save(newFirm);
            });
        
        // Create lawyer
        Lawyer lawyer = new Lawyer();
        lawyer.setFirstName(registerRequest.getFirstName());
        lawyer.setLastName(registerRequest.getLastName());
        lawyer.setEmail(registerRequest.getEmail());
        lawyer.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        lawyer.setPhoneNumber(registerRequest.getPhoneNumber());
        lawyer.setSpecialization(registerRequest.getSpecialization());
        lawyer.setBarNumber(registerRequest.getBarNumber());
        lawyer.setYearsOfExperience(registerRequest.getYearsOfExperience());
        lawyer.setFirm(firm);
        lawyer.setIsActive(true);
        
        Lawyer savedLawyer = lawyerRepository.save(lawyer);
        
        // Generate token
        String jwt = jwtUtils.generateTokenFromEmail(savedLawyer.getEmail());
        
        return new AuthResponse(jwt, savedLawyer);
    }
    
    public boolean isEmailAvailable(String email) {
        return !lawyerRepository.existsByEmail(email);
    }
}

package com.lexiai.controller;

import com.lexiai.model.UserCase;
import com.lexiai.model.UserCase.CaseStatus;
import com.lexiai.model.UserCase.CaseType;
import com.lexiai.service.UserCaseService;
import com.lexiai.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "User Case Management", description = "APIs for managing user cases")
public class UserCaseController {

    @Autowired
    private UserCaseService userCaseService;

    @GetMapping
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Get all cases for current user", description = "Retrieve all cases assigned to the current lawyer")
    @ApiResponse(responseCode = "200", description = "Cases retrieved successfully")
    public ResponseEntity<List<UserCase>> getAllCases() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        List<UserCase> cases = userCaseService.getCasesByLawyerEmail(userPrincipal.getEmail());
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Get paginated cases", description = "Retrieve cases with pagination")
    public ResponseEntity<Page<UserCase>> getCasesPaginated(Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        Page<UserCase> cases = userCaseService.getCasesByLawyerEmailPaginated(userPrincipal.getEmail(), pageable);
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Get case by ID", description = "Retrieve a specific case by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case found"),
        @ApiResponse(responseCode = "404", description = "Case not found")
    })
    public ResponseEntity<UserCase> getCaseById(@PathVariable Long id) {
        UserCase userCase = userCaseService.getCaseById(id);
        return ResponseEntity.ok(userCase);
    }

    @PostMapping
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Create new case", description = "Create a new case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Case created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<UserCase> createCase(@Valid @RequestBody UserCase userCase) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        UserCase createdCase = userCaseService.createCase(userCase, userPrincipal.getEmail());
        return ResponseEntity.status(201).body(createdCase);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Update case", description = "Update an existing case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case updated successfully"),
        @ApiResponse(responseCode = "404", description = "Case not found")
    })
    public ResponseEntity<UserCase> updateCase(@PathVariable Long id, @Valid @RequestBody UserCase userCase) {
        UserCase updatedCase = userCaseService.updateCase(id, userCase);
        return ResponseEntity.ok(updatedCase);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Delete case", description = "Delete a case")
    @ApiResponse(responseCode = "204", description = "Case deleted successfully")
    public ResponseEntity<Void> deleteCase(@PathVariable Long id) {
        userCaseService.deleteCase(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Archive case", description = "Archive a case")
    public ResponseEntity<UserCase> archiveCase(@PathVariable Long id) {
        UserCase archivedCase = userCaseService.archiveCase(id);
        return ResponseEntity.ok(archivedCase);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Update case status", description = "Update the status of a case")
    public ResponseEntity<UserCase> updateCaseStatus(@PathVariable Long id, @RequestParam CaseStatus status) {
        UserCase updatedCase = userCaseService.updateCaseStatus(id, status);
        return ResponseEntity.ok(updatedCase);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Get cases by status", description = "Retrieve cases by their status")
    public ResponseEntity<List<UserCase>> getCasesByStatus(@PathVariable CaseStatus status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        List<UserCase> cases = userCaseService.getCasesByStatusAndLawyer(status, userPrincipal.getEmail());
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Get cases by type", description = "Retrieve cases by their type")
    public ResponseEntity<List<UserCase>> getCasesByType(@PathVariable CaseType type) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        List<UserCase> cases = userCaseService.getCasesByTypeAndLawyer(type, userPrincipal.getEmail());
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Search cases", description = "Search cases by keyword")
    public ResponseEntity<List<UserCase>> searchCases(@RequestParam String keyword) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        List<UserCase> cases = userCaseService.searchCasesByLawyer(keyword, userPrincipal.getEmail());
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/upcoming-hearings")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Get upcoming hearings", description = "Get cases with upcoming hearings")
    public ResponseEntity<List<UserCase>> getUpcomingHearings(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        if (startDate == null) startDate = LocalDateTime.now();
        if (endDate == null) endDate = LocalDateTime.now().plusDays(30);
        
        List<UserCase> cases = userCaseService.getUpcomingHearingsByLawyer(
            userPrincipal.getEmail(), startDate, endDate);
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Get case statistics", description = "Get statistics for user's cases")
    public ResponseEntity<Map<String, Object>> getCaseStatistics() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        Map<String, Object> statistics = userCaseService.getCaseStatisticsByLawyer(userPrincipal.getEmail());
        return ResponseEntity.ok(statistics);
    }

    // Firm-wide endpoints
    @GetMapping("/firm")
    @PreAuthorize("hasRole('FIRM_MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get all firm cases", description = "Retrieve all cases for the firm")
    public ResponseEntity<List<UserCase>> getAllFirmCases() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        List<UserCase> cases = userCaseService.getCasesByFirm(userPrincipal.getEmail());
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/firm/statistics")
    @PreAuthorize("hasRole('FIRM_MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get firm case statistics", description = "Get statistics for firm's cases")
    public ResponseEntity<Map<String, Object>> getFirmCaseStatistics() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        Map<String, Object> statistics = userCaseService.getFirmCaseStatistics(userPrincipal.getEmail());
        return ResponseEntity.ok(statistics);
    }
}

package com.lexiai.controller;

import com.lexiai.dto.CaseSearchRequest;
import com.lexiai.dto.CaseSearchResponse;
import com.lexiai.service.CaseSearchService;
import com.lexiai.service.GPTEnhancementService;
import com.lexiai.model.LegalCase;
import com.lexiai.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CaseSearchController {

    @Autowired
    private CaseSearchService caseSearchService;
    
    @Autowired
    private GPTEnhancementService gptEnhancementService;

    @PostMapping("/cases")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<CaseSearchResponse> searchCases(@Valid @RequestBody CaseSearchRequest request) {
        CaseSearchResponse response = caseSearchService.searchCases(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/cases")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<CaseSearchResponse> searchCasesGet(
            @RequestParam String q,
            @RequestParam(required = false) String caseType,
            @RequestParam(required = false) String courtName,
            @RequestParam(required = false) String jurisdiction,
            @RequestParam(required = false, defaultValue = "keyword") String searchType,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        
        CaseSearchRequest request = new CaseSearchRequest();
        request.setQuery(q);
        request.setCaseType(caseType);
        request.setCourtName(courtName);
        request.setJurisdiction(jurisdiction);
        request.setSearchType(searchType);
        request.setPage(page);
        request.setSize(size);
        
        CaseSearchResponse response = caseSearchService.searchCases(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/cases/{id}")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<LegalCase> getCaseById(@PathVariable Long id) {
        LegalCase legalCase = caseSearchService.getCaseById(id);
        if (legalCase != null) {
            return ResponseEntity.ok(legalCase);
        } else {
            throw new ResourceNotFoundException("Legal Case", "id", id);
        }
    }
    
    @GetMapping("/cases/number/{caseNumber}")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<LegalCase> getCaseByCaseNumber(@PathVariable String caseNumber) {
        LegalCase legalCase = caseSearchService.getCaseByCaseNumber(caseNumber);
        if (legalCase != null) {
            return ResponseEntity.ok(legalCase);
        } else {
            throw new ResourceNotFoundException("Legal Case", "caseNumber", caseNumber);
        }
    }
    
    @GetMapping("/cases/popular")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<List<LegalCase>> getPopularCases(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        List<LegalCase> popularCases = caseSearchService.getPopularCases(limit);
        return ResponseEntity.ok(popularCases);
    }
    
    @GetMapping("/cases/recent")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<List<LegalCase>> getRecentCases(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        List<LegalCase> recentCases = caseSearchService.getRecentCases(limit);
        return ResponseEntity.ok(recentCases);
    }
    
    @GetMapping("/suggestions")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<List<String>> getSearchSuggestions(
            @RequestParam String query) {
        List<String> suggestions = gptEnhancementService.generateSearchSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }
}

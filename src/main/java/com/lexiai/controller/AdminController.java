package com.lexiai.controller;

import com.lexiai.model.Lawyer;
import com.lexiai.model.SearchHistory;
import com.lexiai.repository.LawyerRepository;
import com.lexiai.repository.SearchHistoryRepository;
import com.lexiai.repository.LegalCaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {
    
    @Autowired
    private LawyerRepository lawyerRepository;
    
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;
    
    @Autowired
    private LegalCaseRepository legalCaseRepository;
    
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStats> getDashboardStats() {
        AdminStats stats = new AdminStats();
        
        // Get total users
        stats.setTotalUsers(lawyerRepository.count());
        
        // Get active users (logged in within last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        stats.setActiveUsers(lawyerRepository.countByLastLoginAfter(thirtyDaysAgo));
        
        // Get total searches
        stats.setTotalSearches(searchHistoryRepository.count());
        
        // Get searches today
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        stats.setSearchesToday(searchHistoryRepository.countBySearchDateAfter(startOfDay));
        
        // Get total cases in database
        stats.setTotalCases(legalCaseRepository.count());
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Lawyer>> getAllUsers(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "50") int size) {
        
        List<Lawyer> users = lawyerRepository.findAll();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/analytics/search-volume")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getSearchVolumeData(
            @RequestParam(required = false, defaultValue = "7") int days) {
        
        List<Object[]> searchData = searchHistoryRepository.getSearchVolumeByDay(days);
        List<Map<String, Object>> result = searchData.stream()
            .map(row -> {
                Map<String, Object> data = new HashMap<>();
                data.put("date", row[0].toString());
                data.put("searches", row[1]);
                return data;
            })
            .toList();
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/analytics/court-distribution")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getCourtDistribution() {
        List<Object[]> courtData = searchHistoryRepository.getSearchDistributionByCourt();
        List<Map<String, Object>> result = courtData.stream()
            .map(row -> {
                Map<String, Object> data = new HashMap<>();
                data.put("court", row[0] != null ? row[0].toString() : "Unknown");
                data.put("count", row[1]);
                return data;
            })
            .toList();
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/users/{userId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Long userId) {
        return lawyerRepository.findById(userId)
            .map(lawyer -> {
                lawyer.setIsActive(!lawyer.getIsActive());
                lawyerRepository.save(lawyer);
                return ResponseEntity.ok("User status updated successfully");
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/system/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getSystemLogs(
            @RequestParam(required = false, defaultValue = "100") int limit) {
        
        // Get recent search history as system logs
        List<SearchHistory> recentSearches = searchHistoryRepository
            .findAllByOrderBySearchDateDesc()
            .stream()
            .limit(limit)
            .toList();
        
        List<Map<String, Object>> logs = recentSearches.stream()
            .map(search -> {
                Map<String, Object> log = new HashMap<>();
                log.put("timestamp", search.getSearchDate());
                log.put("user", search.getLawyer().getFullName());
                log.put("action", "SEARCH");
                log.put("query", search.getSearchQuery());
                log.put("results", search.getResultsCount());
                log.put("responseTime", search.getResponseTimeMs());
                return log;
            })
            .toList();
        
        return ResponseEntity.ok(logs);
    }
    
    // Inner class for admin statistics
    public static class AdminStats {
        private Long totalUsers;
        private Long activeUsers;
        private Long totalSearches;
        private Long searchesToday;
        private Long totalCases;
        
        // Getters and setters
        public Long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
        
        public Long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(Long activeUsers) { this.activeUsers = activeUsers; }
        
        public Long getTotalSearches() { return totalSearches; }
        public void setTotalSearches(Long totalSearches) { this.totalSearches = totalSearches; }
        
        public Long getSearchesToday() { return searchesToday; }
        public void setSearchesToday(Long searchesToday) { this.searchesToday = searchesToday; }
        
        public Long getTotalCases() { return totalCases; }
        public void setTotalCases(Long totalCases) { this.totalCases = totalCases; }
    }
}

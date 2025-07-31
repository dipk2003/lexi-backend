package com.lexiai.controller;

import com.lexiai.model.Lawyer;
import com.lexiai.model.SearchHistory;
import com.lexiai.model.User;
import com.lexiai.repository.LawyerRepository;
import com.lexiai.repository.SearchHistoryRepository;
import com.lexiai.service.UserService;
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

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    @Autowired
    private LawyerRepository lawyerRepository;
    
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;
    
    @GetMapping("/profile")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Lawyer> getCurrentUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        Optional<Lawyer> lawyer = lawyerRepository.findByEmail(userPrincipal.getEmail());
        if (lawyer.isPresent()) {
            return ResponseEntity.ok(lawyer.get());
        } else {
            throw new ResourceNotFoundException("Lawyer", "email", userPrincipal.getEmail());
        }
    }
    
    @PutMapping("/profile")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Lawyer> updateProfile(@RequestBody Lawyer updatedLawyer) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        Optional<Lawyer> lawyerOpt = lawyerRepository.findByEmail(userPrincipal.getEmail());
        if (lawyerOpt.isPresent()) {
            Lawyer lawyer = lawyerOpt.get();
            
            // Update allowed fields (don't allow changing email, password, or firm)
            lawyer.setFirstName(updatedLawyer.getFirstName());
            lawyer.setLastName(updatedLawyer.getLastName());
            lawyer.setPhoneNumber(updatedLawyer.getPhoneNumber());
            lawyer.setSpecialization(updatedLawyer.getSpecialization());
            lawyer.setBarNumber(updatedLawyer.getBarNumber());
            lawyer.setYearsOfExperience(updatedLawyer.getYearsOfExperience());
            
            Lawyer savedLawyer = lawyerRepository.save(lawyer);
            return ResponseEntity.ok(savedLawyer);
        } else {
            throw new ResourceNotFoundException("Lawyer", "email", userPrincipal.getEmail());
        }
    }
    
    @GetMapping("/search-history")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<List<SearchHistory>> getSearchHistory(
            @RequestParam(required = false, defaultValue = "50") int limit) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        Optional<Lawyer> lawyer = lawyerRepository.findByEmail(userPrincipal.getEmail());
        if (lawyer.isPresent()) {
            List<SearchHistory> history = searchHistoryRepository
                .findByLawyerIdOrderBySearchDateDesc(lawyer.get().getId())
                .stream()
                .limit(limit)
                .toList();
            return ResponseEntity.ok(history);
        } else {
            throw new ResourceNotFoundException("Lawyer", "email", userPrincipal.getEmail());
        }
    }
    
    @GetMapping("/search-stats")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<SearchStats> getSearchStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        Optional<Lawyer> lawyer = lawyerRepository.findByEmail(userPrincipal.getEmail());
        if (lawyer.isPresent()) {
            Long totalSearches = searchHistoryRepository.countByLawyerId(lawyer.get().getId());
            List<Object[]> topQueries = searchHistoryRepository
                .findTopSearchQueriesByLawyer(lawyer.get().getId())
                .stream()
                .limit(5)
                .toList();
            
            SearchStats stats = new SearchStats();
            stats.setTotalSearches(totalSearches);
            stats.setTopQueries(topQueries);
            
            return ResponseEntity.ok(stats);
        } else {
            throw new ResourceNotFoundException("Lawyer", "email", userPrincipal.getEmail());
        }
    }
    
    @PostMapping("/settings")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<String> updateSettings(@RequestBody UserSettings settings) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        Optional<Lawyer> lawyer = lawyerRepository.findByEmail(userPrincipal.getEmail());
        if (lawyer.isPresent()) {
            Lawyer lawyerEntity = lawyer.get();
            
            // Update lawyer settings (we'll store as JSON in a settings field)
            lawyerEntity.setNotificationPreferences(settings.toJson());
            lawyerRepository.save(lawyerEntity);
            
            return ResponseEntity.ok("Settings updated successfully");
        } else {
            throw new ResourceNotFoundException("Lawyer", "email", userPrincipal.getEmail());
        }
    }
    
    @GetMapping("/settings")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<UserSettings> getSettings() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        Optional<Lawyer> lawyer = lawyerRepository.findByEmail(userPrincipal.getEmail());
        if (lawyer.isPresent()) {
            Lawyer lawyerEntity = lawyer.get();
            
            // Parse settings from JSON or return defaults
            UserSettings settings = UserSettings.fromJson(lawyerEntity.getNotificationPreferences());
            return ResponseEntity.ok(settings);
        } else {
            throw new ResourceNotFoundException("Lawyer", "email", userPrincipal.getEmail());
        }
    }
    
    // Inner class for search statistics
    public static class SearchStats {
        private Long totalSearches;
        private List<Object[]> topQueries;
        
        public Long getTotalSearches() { return totalSearches; }
        public void setTotalSearches(Long totalSearches) { this.totalSearches = totalSearches; }
        
        public List<Object[]> getTopQueries() { return topQueries; }
        public void setTopQueries(List<Object[]> topQueries) { this.topQueries = topQueries; }
    }
    
    // Inner class for user settings
    public static class UserSettings {
        private NotificationSettings notifications;
        private PreferenceSettings preferences;
        
        public UserSettings() {
            this.notifications = new NotificationSettings();
            this.preferences = new PreferenceSettings();
        }
        
        public String toJson() {
            // Simple JSON serialization - in production, use Jackson
            return String.format(
                "{\"notifications\":{\"email\":%b,\"desktop\":%b,\"research\":%b},\"preferences\":{\"theme\":\"%s\",\"language\":\"%s\",\"defaultSortBy\":\"%s\"}}",
                notifications.email, notifications.desktop, notifications.research,
                preferences.theme, preferences.language, preferences.defaultSortBy
            );
        }
        
        public static UserSettings fromJson(String json) {
            UserSettings settings = new UserSettings();
            if (json == null || json.isEmpty()) {
                return settings; // Return defaults
            }
            // Simple JSON parsing - in production, use Jackson
            // For now, return defaults
            return settings;
        }
        
        // Getters and setters
        public NotificationSettings getNotifications() { return notifications; }
        public void setNotifications(NotificationSettings notifications) { this.notifications = notifications; }
        
        public PreferenceSettings getPreferences() { return preferences; }
        public void setPreferences(PreferenceSettings preferences) { this.preferences = preferences; }
        
        public static class NotificationSettings {
            private boolean email = true;
            private boolean desktop = false;
            private boolean research = true;
            
            // Getters and setters
            public boolean isEmail() { return email; }
            public void setEmail(boolean email) { this.email = email; }
            
            public boolean isDesktop() { return desktop; }
            public void setDesktop(boolean desktop) { this.desktop = desktop; }
            
            public boolean isResearch() { return research; }
            public void setResearch(boolean research) { this.research = research; }
        }
        
        public static class PreferenceSettings {
            private String theme = "light";
            private String language = "en";
            private String defaultSortBy = "relevance";
            
            // Getters and setters
            public String getTheme() { return theme; }
            public void setTheme(String theme) { this.theme = theme; }
            
            public String getLanguage() { return language; }
            public void setLanguage(String language) { this.language = language; }
            
            public String getDefaultSortBy() { return defaultSortBy; }
            public void setDefaultSortBy(String defaultSortBy) { this.defaultSortBy = defaultSortBy; }
        }
    }
}

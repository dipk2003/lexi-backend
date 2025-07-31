package com.lexiai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PublicController {
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "LexiAI Backend");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "LexiAI - Legal Research Tool");
        info.put("description", "AI-powered legal research tool for Indian courts");
        info.put("features", new String[]{
            "Case search across Indian courts",
            "Web scraping from official court websites",
            "Search history tracking",
            "User authentication with JWT",
            "Firm and lawyer management"
        });
        info.put("endpoints", new String[]{
            "/api/auth/login",
            "/api/auth/register",
            "/api/search/cases",
            "/api/user/profile"
        });
        return ResponseEntity.ok(info);
    }
}

package com.lexiai.service;

import com.lexiai.model.LegalCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GPTEnhancementService {

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openAiApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GPTEnhancementService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Enhances legal case data with GPT analysis
     */
    public List<LegalCase> enhanceCasesWithGPT(List<LegalCase> cases, String searchQuery) {
        if (cases.isEmpty() || openAiApiKey == null || openAiApiKey.isEmpty()) {
            log.warn("GPT enhancement skipped - no cases or API key not configured");
            return cases;
        }

        try {
            // Process cases in batches to avoid token limits
            return cases.stream()
                    .map(legalCase -> enhanceSingleCase(legalCase, searchQuery))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error enhancing cases with GPT: {}", e.getMessage());
            return cases; // Return original cases if enhancement fails
        }
    }

    /**
     * Enhances a single legal case with GPT analysis
     */
    private LegalCase enhanceSingleCase(LegalCase legalCase, String searchQuery) {
        try {
            String prompt = buildEnhancementPrompt(legalCase, searchQuery);
            String gptResponse = callGPTAPI(prompt);
            
            if (gptResponse != null && !gptResponse.isEmpty()) {
                // Parse GPT response and enhance the case
                Map<String, String> enhancements = parseGPTResponse(gptResponse);
                applyEnhancements(legalCase, enhancements);
            }
            
            return legalCase;
        } catch (Exception e) {
            log.error("Error enhancing case {}: {}", legalCase.getCaseNumber(), e.getMessage());
            return legalCase;
        }
    }

    /**
     * Builds a prompt for GPT to analyze and enhance legal case data
     */
    private String buildEnhancementPrompt(LegalCase legalCase, String searchQuery) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a legal AI assistant. Analyze the following legal case and provide enhancements:\n\n");
        prompt.append("Search Query: ").append(searchQuery).append("\n");
        prompt.append("Case Title: ").append(legalCase.getTitle()).append("\n");
        prompt.append("Case Number: ").append(legalCase.getCaseNumber()).append("\n");
        prompt.append("Court: ").append(legalCase.getCourtName()).append("\n");
        prompt.append("Date: ").append(legalCase.getDate()).append("\n");
        prompt.append("Summary: ").append(legalCase.getSummary()).append("\n\n");
        
        prompt.append("Please provide the following enhancements in JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"relevanceScore\": \"0-100 score of relevance to search query\",\n");
        prompt.append("  \"keyPoints\": \"3-5 key legal points from this case\",\n");
        prompt.append("  \"legalPrinciples\": \"Main legal principles established\",\n");
        prompt.append("  \"practicalImplications\": \"Practical implications for lawyers\",\n");
        prompt.append("  \"relatedConcepts\": \"Related legal concepts and keywords\",\n");
        prompt.append("  \"enhancedSummary\": \"Improved, more comprehensive summary\"\n");
        prompt.append("}\n\n");
        prompt.append("Keep responses concise and legally accurate.");
        
        return prompt.toString();
    }

    /**
     * Calls OpenAI GPT API
     */
    private String callGPTAPI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("max_tokens", 800);
            requestBody.put("temperature", 0.3);
            
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            requestBody.put("messages", List.of(message));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    openAiApiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return extractContentFromResponse(response.getBody());
            } else {
                log.error("GPT API returned status: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error calling GPT API: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts content from GPT API response
     */
    private String extractContentFromResponse(String responseBody) {
        try {
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        } catch (Exception e) {
            log.error("Error parsing GPT response: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Parses GPT response JSON
     */
    private Map<String, String> parseGPTResponse(String gptResponse) {
        try {
            // Extract JSON from response (in case there's extra text)
            String jsonStart = gptResponse.indexOf("{") >= 0 ? gptResponse.substring(gptResponse.indexOf("{")) : gptResponse;
            String jsonEnd = jsonStart.lastIndexOf("}") >= 0 ? jsonStart.substring(0, jsonStart.lastIndexOf("}") + 1) : jsonStart;
            
            return objectMapper.readValue(jsonEnd, Map.class);
        } catch (Exception e) {
            log.error("Error parsing GPT response JSON: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Applies GPT enhancements to the legal case
     */
    private void applyEnhancements(LegalCase legalCase, Map<String, String> enhancements) {
        try {
            // Set relevance score
            if (enhancements.containsKey("relevanceScore")) {
                try {
                    int score = Integer.parseInt(enhancements.get("relevanceScore").replaceAll("[^0-9]", ""));
                    legalCase.setRelevanceScore(score);
                } catch (NumberFormatException e) {
                    log.warn("Invalid relevance score format");
                }
            }

            // Enhance summary if provided
            if (enhancements.containsKey("enhancedSummary")) {
                String enhancedSummary = enhancements.get("enhancedSummary");
                if (enhancedSummary != null && !enhancedSummary.isEmpty()) {
                    legalCase.setSummary(enhancedSummary);
                }
            }

            // Set key points
            if (enhancements.containsKey("keyPoints")) {
                legalCase.setKeyPoints(enhancements.get("keyPoints"));
            }

            // Set legal principles
            if (enhancements.containsKey("legalPrinciples")) {
                legalCase.setLegalPrinciples(enhancements.get("legalPrinciples"));
            }

            // Set practical implications
            if (enhancements.containsKey("practicalImplications")) {
                legalCase.setPracticalImplications(enhancements.get("practicalImplications"));
            }

            // Set related concepts
            if (enhancements.containsKey("relatedConcepts")) {
                legalCase.setRelatedConcepts(enhancements.get("relatedConcepts"));
            }

            // Mark as AI enhanced
            legalCase.setAiEnhanced(true);
            
            log.info("Successfully enhanced case: {}", legalCase.getCaseNumber());
        } catch (Exception e) {
            log.error("Error applying enhancements to case: {}", e.getMessage());
        }
    }

    /**
     * Generates search suggestions based on query
     */
    public List<String> generateSearchSuggestions(String query) {
        if (openAiApiKey == null || openAiApiKey.isEmpty()) {
            return List.of();
        }

        try {
            String prompt = "Based on the legal search query: '" + query + "', suggest 5 related legal search terms or case types that might be relevant. Return only the suggestions, one per line.";
            String response = callGPTAPI(prompt);
            
            if (response != null) {
                return List.of(response.split("\n")).stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .limit(5)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Error generating search suggestions: {}", e.getMessage());
        }
        
        return List.of();
    }
}

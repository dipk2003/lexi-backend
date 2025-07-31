package com.lexiai.service;

import com.lexiai.dto.CaseSearchRequest;
import com.lexiai.dto.CaseSearchResponse;
import com.lexiai.model.LegalCase;
import com.lexiai.model.SearchHistory;
import com.lexiai.model.Lawyer;
import com.lexiai.repository.LegalCaseRepository;
import com.lexiai.repository.SearchHistoryRepository;
import com.lexiai.repository.LawyerRepository;
import com.lexiai.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CaseSearchService {

    @Autowired
    private LegalCaseRepository legalCaseRepository;
    
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;
    
    @Autowired
    private LawyerRepository lawyerRepository;
    
    @Autowired
    private IndianCourtScraperService scraperService;
    
    @Autowired
    private AlternativeLegalScraperService alternativeScraperService;
    
    @Autowired
    private GPTEnhancementService gptEnhancementService;

    @Transactional
    public CaseSearchResponse searchCases(CaseSearchRequest request) {
        long startTime = System.currentTimeMillis();
        
        // First search in database with pagination
        List<LegalCase> databaseResults = searchInDatabase(request);
        
        List<LegalCase> allResults = new ArrayList<>(databaseResults);
        String dataSource = "database";
        
        // Only trigger web scraping if no results found AND it's a keyword search
        // Avoid scraping for case number or party searches (they're usually specific)
        boolean shouldScrape = databaseResults.isEmpty() && 
                              ("keyword".equals(request.getSearchType()) || request.getSearchType() == null);
        
        if (shouldScrape) {
            List<LegalCase> scrapedResults = searchExternal(request);
            if (!scrapedResults.isEmpty()) {
                // Enhance scraped results with GPT before saving
                log.info("🤖 Enhancing {} scraped cases with GPT analysis", scrapedResults.size());
                List<LegalCase> enhancedResults = gptEnhancementService.enhanceCasesWithGPT(scrapedResults, request.getQuery());
                
                // Save enhanced results to the database immediately
                saveScrapedResults(enhancedResults);
                allResults.addAll(enhancedResults);
                dataSource = "web_scraping_ai_enhanced";
            }
        }
        
        // Also enhance existing database results if they haven't been AI enhanced yet
        if (!databaseResults.isEmpty()) {
            List<LegalCase> unenhancedCases = databaseResults.stream()
                .filter(legalCase -> legalCase.getAiEnhanced() == null || !legalCase.getAiEnhanced())
                .collect(Collectors.toList());
                
            if (!unenhancedCases.isEmpty()) {
                log.info("🤖 Enhancing {} existing database cases with GPT analysis", unenhancedCases.size());
                gptEnhancementService.enhanceCasesWithGPT(unenhancedCases, request.getQuery());
                // Save the enhanced database results
                legalCaseRepository.saveAll(unenhancedCases);
            }
        }
        
        // Update search count for found cases
        allResults.forEach(LegalCase::incrementSearchCount);
        legalCaseRepository.saveAll(allResults);
        
        // Record search history
        recordSearchHistory(request, allResults.size(), dataSource, System.currentTimeMillis() - startTime);
        
        // Prepare paginated response
        int totalResults = allResults.size();
        int fromIndex = request.getPage() * request.getSize();
        int toIndex = Math.min(fromIndex + request.getSize(), totalResults);
        
        List<LegalCase> paginatedResults = fromIndex < totalResults ? 
            allResults.subList(fromIndex, toIndex) : new ArrayList<>();
        
        CaseSearchResponse response = new CaseSearchResponse();
        response.setCases(paginatedResults);
        response.setTotalResults(totalResults);
        response.setCurrentPage(request.getPage());
        response.setTotalPages((int) Math.ceil((double) totalResults / request.getSize()));
        response.setSearchQuery(request.getQuery());
        response.setDataSource(dataSource);
        response.setResponseTimeMs(System.currentTimeMillis() - startTime);
        response.setHasMoreResults(toIndex < totalResults);
        
        return response;
    }
    
    public LegalCase getCaseById(Long id) {
        Optional<LegalCase> caseOpt = legalCaseRepository.findById(id);
        if (caseOpt.isPresent()) {
            LegalCase legalCase = caseOpt.get();
            legalCase.incrementSearchCount();
            return legalCaseRepository.save(legalCase);
        }
        return null;
    }
    
    public LegalCase getCaseByCaseNumber(String caseNumber) {
        Optional<LegalCase> caseOpt = legalCaseRepository.findByCaseNumber(caseNumber);
        if (caseOpt.isPresent()) {
            LegalCase legalCase = caseOpt.get();
            legalCase.incrementSearchCount();
            return legalCaseRepository.save(legalCase);
        }
        return null;
    }
    
    @Cacheable(value = "popularCases", key = "#limit")
    public List<LegalCase> getPopularCases(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return legalCaseRepository.findMostSearchedCases().stream()
            .limit(limit)
            .toList();
    }
    
    @Cacheable(value = "recentCases", key = "#limit")
    public List<LegalCase> getRecentCases(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return legalCaseRepository.findRecentCases().stream()
            .limit(limit)
            .toList();
    }

    @Cacheable(value = "databaseSearch", key = "#request.query + '_' + #request.searchType + '_' + #request.caseType + '_' + #request.courtName + '_' + #request.jurisdiction")
    private List<LegalCase> searchInDatabase(CaseSearchRequest request) {
        String searchType = request.getSearchType();
        String query = request.getQuery();
        
        if ("case_number".equals(searchType)) {
            Optional<LegalCase> caseOpt = legalCaseRepository.findByCaseNumber(query);
            return caseOpt.map(List::of).orElse(new ArrayList<>());
        } else if ("party".equals(searchType)) {
            return legalCaseRepository.findByParty(query);
        } else if ("title".equals(searchType)) {
            return legalCaseRepository.findByTitleContainingIgnoreCase(query);
        } else {
            // Default to keyword search
            if (request.getCaseType() != null || request.getCourtName() != null || request.getJurisdiction() != null) {
                return legalCaseRepository.findByMultipleCriteria(
                    request.getCaseType(),
                    request.getJurisdiction(),
                    request.getCourtName(),
                    query
                );
            } else {
                return legalCaseRepository.searchByKeyword(query);
            }
        }
    }

    private List<LegalCase> searchExternal(CaseSearchRequest request) {
        List<LegalCase> results = new ArrayList<>();
        String query = request.getQuery();
        String courtName = request.getCourtName();
        
        try {
            // Try Indian Kanoon first (most reliable)
            System.out.println("🔍 Searching Indian Kanoon for: " + query);
            List<LegalCase> indianKanoonResults = scraperService.scrapeIndianKanoon(query);
            if (!indianKanoonResults.isEmpty()) {
                results.addAll(indianKanoonResults);
                System.out.println("✅ Found " + indianKanoonResults.size() + " cases from Indian Kanoon");
            } else {
                System.out.println("⚠️ Indian Kanoon returned no results, trying alternative sources...");
                
                // Fallback to alternative legal databases
                try {
                    List<LegalCase> alternativeResults = alternativeScraperService.searchJudis(query);
                    if (!alternativeResults.isEmpty()) {
                        results.addAll(alternativeResults);
                        System.out.println("✅ Found " + alternativeResults.size() + " cases from alternative sources");
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Alternative scraping also failed: " + e.getMessage());
                }
            }
            
            // If we need more results, try other sources
            if (results.size() < 5) {
                try {
                    if ("case_number".equals(request.getSearchType())) {
                        // If it looks like a case number, try specific court scrapers
                        if (courtName != null) {
                            if (courtName.toLowerCase().contains("supreme")) {
                                results.addAll(scraperService.scrapeSupremeCourt(query));
                            } else {
                                results.addAll(scraperService.scrapeHighCourt(query, courtName));
                            }
                        } else {
                            // Try eCourts
                            results.addAll(scraperService.scrapeECourts(query, "Generic"));
                        }
                    } else {
                        // For other search types, try eCourts
                        results.addAll(scraperService.scrapeECourts(query, courtName != null ? courtName : "Generic"));
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Secondary scraping failed: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            // Log error but don't fail the entire search
            System.err.println("❌ Web scraping failed: " + e.getMessage());
        }
        
        return results;
    }
    
    private void saveScrapedResults(List<LegalCase> scrapedResults) {
        try {
            for (LegalCase legalCase : scrapedResults) {
                // Check if case already exists to avoid duplicates
                if (legalCase.getCaseNumber() != null) {
                    Optional<LegalCase> existing = legalCaseRepository.findByCaseNumber(legalCase.getCaseNumber());
                    if (existing.isEmpty()) {
                        legalCaseRepository.save(legalCase);
                    }
                } else {
                    legalCaseRepository.save(legalCase);
                }
            }
            log.info("Saved {} scraped cases to database", scrapedResults.size());
        } catch (Exception e) {
            log.error("Failed to save scraped results: {}", e.getMessage());
        }
    }
    
    private void recordSearchHistory(CaseSearchRequest request, int resultsCount, String dataSource, long responseTime) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
                Optional<Lawyer> lawyerOpt = lawyerRepository.findByEmail(userPrincipal.getEmail());
                
                if (lawyerOpt.isPresent()) {
                    SearchHistory history = new SearchHistory();
                    history.setSearchQuery(request.getQuery());
                    history.setSearchType(request.getSearchType());
                    history.setResultsCount(resultsCount);
                    history.setDataSource(dataSource);
                    history.setResponseTimeMs(responseTime);
                    history.setLawyer(lawyerOpt.get());
                    
                    searchHistoryRepository.save(history);
                }
            }
        } catch (Exception e) {
            // Don't fail the search if history recording fails
            System.err.println("Failed to record search history: " + e.getMessage());
        }
    }
}

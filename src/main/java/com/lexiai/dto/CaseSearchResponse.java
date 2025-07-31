package com.lexiai.dto;

import com.lexiai.model.LegalCase;
import java.util.List;

public class CaseSearchResponse {
    
    private List<LegalCase> cases;
    private int totalResults;
    private int currentPage;
    private int totalPages;
    private String searchQuery;
    private String dataSource; // "database", "web_scraping", "mixed"
    private long responseTimeMs;
    private boolean hasMoreResults;
    
    // Constructors
    public CaseSearchResponse() {}
    
    public CaseSearchResponse(List<LegalCase> cases, int totalResults, int currentPage, int totalPages) {
        this.cases = cases;
        this.totalResults = totalResults;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.hasMoreResults = currentPage < totalPages - 1;
    }
    
    // Getters and Setters
    public List<LegalCase> getCases() { return cases; }
    public void setCases(List<LegalCase> cases) { this.cases = cases; }
    
    public int getTotalResults() { return totalResults; }
    public void setTotalResults(int totalResults) { this.totalResults = totalResults; }
    
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
    
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    
    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    
    public boolean isHasMoreResults() { return hasMoreResults; }
    public void setHasMoreResults(boolean hasMoreResults) { this.hasMoreResults = hasMoreResults; }
}

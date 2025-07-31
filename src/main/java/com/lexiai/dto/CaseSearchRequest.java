package com.lexiai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CaseSearchRequest {
    
    @NotBlank(message = "Search query is required")
    @Size(min = 2, max = 255, message = "Search query must be between 2 and 255 characters")
    private String query;
    
    private String caseType;
    private String courtName;
    private String jurisdiction;
    private String searchType; // "case_number", "title", "party", "keyword"
    private int page = 0;
    private int size = 10;
    
    // Constructors
    public CaseSearchRequest() {}
    
    public CaseSearchRequest(String query) {
        this.query = query;
    }
    
    // Getters and Setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    
    public String getCaseType() { return caseType; }
    public void setCaseType(String caseType) { this.caseType = caseType; }
    
    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }
    
    public String getJurisdiction() { return jurisdiction; }
    public void setJurisdiction(String jurisdiction) { this.jurisdiction = jurisdiction; }
    
    public String getSearchType() { return searchType; }
    public void setSearchType(String searchType) { this.searchType = searchType; }
    
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}

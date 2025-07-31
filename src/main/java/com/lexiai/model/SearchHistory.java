package com.lexiai.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_histories")
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "search_query", nullable = false)
    private String searchQuery;

    @Column(name = "search_date", nullable = false)
    private LocalDateTime searchDate;

    @Column(name = "results_count")
    private Integer resultsCount;

    @Column(name = "search_type") // e.g., "case_title", "case_number", "keyword"
    private String searchType;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "data_source") // e.g., "database", "external_api", "web_scraping"
    private String dataSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_id", nullable = false)
    @JsonBackReference
    private Lawyer lawyer;

    // Constructors
    public SearchHistory() {
        this.searchDate = LocalDateTime.now();
    }

    public SearchHistory(String searchQuery, Lawyer lawyer) {
        this();
        this.searchQuery = searchQuery;
        this.lawyer = lawyer;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }

    public LocalDateTime getSearchDate() { return searchDate; }
    public void setSearchDate(LocalDateTime searchDate) { this.searchDate = searchDate; }

    public Integer getResultsCount() { return resultsCount; }
    public void setResultsCount(Integer resultsCount) { this.resultsCount = resultsCount; }

    public String getSearchType() { return searchType; }
    public void setSearchType(String searchType) { this.searchType = searchType; }

    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public Lawyer getLawyer() { return lawyer; }
    public void setLawyer(Lawyer lawyer) { this.lawyer = lawyer; }
}

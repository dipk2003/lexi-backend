package com.lexiai.repository;

import com.lexiai.model.LegalCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LegalCaseRepository extends JpaRepository<LegalCase, Long> {
    
    // Basic search methods
    List<LegalCase> findByTitleContainingIgnoreCase(String keyword);
    
    Optional<LegalCase> findByCaseNumber(String caseNumber);
    
    List<LegalCase> findByCaseType(String caseType);
    
    List<LegalCase> findByCourtNameContainingIgnoreCase(String courtName);
    
    List<LegalCase> findByJurisdiction(String jurisdiction);
    
    List<LegalCase> findByCaseStatus(String caseStatus);
    
    // Advanced search methods
    @Query("SELECT lc FROM LegalCase lc WHERE " +
           "LOWER(lc.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(lc.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(lc.caseSummary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(lc.keyIssues) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(lc.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<LegalCase> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT lc FROM LegalCase lc WHERE " +
           "LOWER(lc.plaintiff) LIKE LOWER(CONCAT('%', :party, '%')) OR " +
           "LOWER(lc.defendant) LIKE LOWER(CONCAT('%', :party, '%'))")
    List<LegalCase> findByParty(@Param("party") String party);
    
    @Query("SELECT lc FROM LegalCase lc WHERE lc.judgeName LIKE %:judgeName%")
    List<LegalCase> findByJudgeNameContainingIgnoreCase(@Param("judgeName") String judgeName);
    
    @Query("SELECT lc FROM LegalCase lc WHERE lc.filingDate BETWEEN :startDate AND :endDate")
    List<LegalCase> findByFilingDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT lc FROM LegalCase lc WHERE lc.decisionDate BETWEEN :startDate AND :endDate")
    List<LegalCase> findByDecisionDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Complex search with multiple criteria
    @Query("SELECT lc FROM LegalCase lc WHERE " +
           "(:caseType IS NULL OR lc.caseType = :caseType) AND " +
           "(:jurisdiction IS NULL OR lc.jurisdiction = :jurisdiction) AND " +
           "(:courtName IS NULL OR LOWER(lc.courtName) LIKE LOWER(CONCAT('%', :courtName, '%'))) AND " +
           "(:keyword IS NULL OR " +
           " LOWER(lc.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(lc.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(lc.caseSummary) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<LegalCase> findByMultipleCriteria(@Param("caseType") String caseType,
                                          @Param("jurisdiction") String jurisdiction,
                                          @Param("courtName") String courtName,
                                          @Param("keyword") String keyword);
    
    // Popular cases
    @Query("SELECT lc FROM LegalCase lc ORDER BY lc.searchCount DESC")
    List<LegalCase> findMostSearchedCases();
    
    // Recent cases
    @Query("SELECT lc FROM LegalCase lc ORDER BY lc.createdAt DESC")
    List<LegalCase> findRecentCases();
    
    // Statistics
    @Query("SELECT COUNT(lc) FROM LegalCase lc WHERE lc.caseType = :caseType")
    Long countByCaseType(@Param("caseType") String caseType);
    
    @Query("SELECT COUNT(lc) FROM LegalCase lc WHERE lc.jurisdiction = :jurisdiction")
    Long countByJurisdiction(@Param("jurisdiction") String jurisdiction);
}


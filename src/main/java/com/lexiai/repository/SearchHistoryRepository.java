package com.lexiai.repository;

import com.lexiai.model.SearchHistory;
import com.lexiai.model.Lawyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    
    List<SearchHistory> findByLawyer(Lawyer lawyer);
    
    List<SearchHistory> findByLawyerId(Long lawyerId);
    
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.lawyer.id = :lawyerId ORDER BY sh.searchDate DESC")
    List<SearchHistory> findByLawyerIdOrderBySearchDateDesc(@Param("lawyerId") Long lawyerId);
    
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.lawyer.id = :lawyerId AND sh.searchDate BETWEEN :startDate AND :endDate")
    List<SearchHistory> findByLawyerIdAndDateRange(@Param("lawyerId") Long lawyerId, 
                                                  @Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT sh.searchQuery, COUNT(sh) as count FROM SearchHistory sh WHERE sh.lawyer.id = :lawyerId GROUP BY sh.searchQuery ORDER BY count DESC")
    List<Object[]> findTopSearchQueriesByLawyer(@Param("lawyerId") Long lawyerId);
    
    @Query("SELECT COUNT(sh) FROM SearchHistory sh WHERE sh.lawyer.id = :lawyerId")
    Long countByLawyerId(@Param("lawyerId") Long lawyerId);
    
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.lawyer.firm.id = :firmId ORDER BY sh.searchDate DESC")
    List<SearchHistory> findByFirmIdOrderBySearchDateDesc(@Param("firmId") Long firmId);
    
    @Query("SELECT COUNT(sh) FROM SearchHistory sh WHERE sh.lawyer.firm.id = :firmId")
    Long countByFirmId(@Param("firmId") Long firmId);
    
    // Admin analytics queries
    @Query("SELECT COUNT(sh) FROM SearchHistory sh WHERE sh.searchDate > :date")
    Long countBySearchDateAfter(@Param("date") LocalDateTime date);
    
    @Query("SELECT DATE(sh.searchDate), COUNT(sh) FROM SearchHistory sh WHERE sh.searchDate >= :startDate GROUP BY DATE(sh.searchDate) ORDER BY DATE(sh.searchDate) DESC")
    List<Object[]> getSearchVolumeByDay(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COALESCE(lc.courtName, 'Unknown'), COUNT(sh) FROM SearchHistory sh LEFT JOIN LegalCase lc ON sh.searchQuery = lc.title GROUP BY lc.courtName ORDER BY COUNT(sh) DESC")
    List<Object[]> getSearchDistributionByCourt();
    
    @Query("SELECT sh FROM SearchHistory sh ORDER BY sh.searchDate DESC")
    List<SearchHistory> findAllByOrderBySearchDateDesc();
    
    // Fix the getSearchVolumeByDay method to accept days parameter
    default List<Object[]> getSearchVolumeByDay(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return getSearchVolumeByDay(startDate);
    }
}

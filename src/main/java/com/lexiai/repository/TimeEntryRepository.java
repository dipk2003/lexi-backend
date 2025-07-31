package com.lexiai.repository;

import com.lexiai.model.TimeEntry;
import com.lexiai.model.TimeEntry.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    
    List<TimeEntry> findByUserCaseId(Long userCaseId);
    
    List<TimeEntry> findByLawyerId(Long lawyerId);
    
    List<TimeEntry> findByActivityType(ActivityType activityType);
    
    List<TimeEntry> findByIsBillable(Boolean isBillable);
    
    List<TimeEntry> findByIsBilled(Boolean isBilled);
    
    List<TimeEntry> findByLawyerIdAndIsBillable(Long lawyerId, Boolean isBillable);
    
    @Query("SELECT t FROM TimeEntry t WHERE t.startTime BETWEEN :startDate AND :endDate")
    List<TimeEntry> findByStartTimeBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM TimeEntry t WHERE t.lawyer.id = :lawyerId AND t.startTime BETWEEN :startDate AND :endDate")
    List<TimeEntry> findByLawyerAndDateRange(@Param("lawyerId") Long lawyerId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM TimeEntry t WHERE t.userCase.id = :caseId AND t.startTime BETWEEN :startDate AND :endDate")
    List<TimeEntry> findByCaseAndDateRange(@Param("caseId") Long caseId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(t.billableHours) FROM TimeEntry t WHERE t.lawyer.id = :lawyerId AND t.isBillable = true")
    BigDecimal getTotalBillableHoursByLawyer(@Param("lawyerId") Long lawyerId);
    
    @Query("SELECT SUM(t.totalAmount) FROM TimeEntry t WHERE t.lawyer.id = :lawyerId AND t.isBillable = true")
    BigDecimal getTotalBillableAmountByLawyer(@Param("lawyerId") Long lawyerId);
    
    @Query("SELECT SUM(t.billableHours) FROM TimeEntry t WHERE t.userCase.id = :caseId AND t.isBillable = true")
    BigDecimal getTotalBillableHoursByCase(@Param("caseId") Long caseId);
    
    @Query("SELECT SUM(t.totalAmount) FROM TimeEntry t WHERE t.userCase.id = :caseId AND t.isBillable = true")
    BigDecimal getTotalBillableAmountByCase(@Param("caseId") Long caseId);
    
    @Query("SELECT t FROM TimeEntry t WHERE t.userCase.firm.id = :firmId AND t.startTime BETWEEN :startDate AND :endDate")
    List<TimeEntry> findByFirmAndDateRange(@Param("firmId") Long firmId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(t.totalAmount) FROM TimeEntry t WHERE t.userCase.firm.id = :firmId AND t.isBillable = true AND t.startTime BETWEEN :startDate AND :endDate")
    BigDecimal getFirmRevenueByDateRange(@Param("firmId") Long firmId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM TimeEntry t WHERE t.isBillable = true AND t.isBilled = false")
    List<TimeEntry> findUnbilledEntries();
    
    @Query("SELECT t FROM TimeEntry t WHERE t.lawyer.id = :lawyerId AND t.isBillable = true AND t.isBilled = false")
    List<TimeEntry> findUnbilledEntriesByLawyer(@Param("lawyerId") Long lawyerId);
}

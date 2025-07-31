package com.lexiai.repository;

import com.lexiai.model.UserCase;
import com.lexiai.model.UserCase.CaseStatus;
import com.lexiai.model.UserCase.CaseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserCaseRepository extends JpaRepository<UserCase, Long> {
    
    List<UserCase> findByLawyerId(Long lawyerId);
    
    List<UserCase> findByFirmId(Long firmId);
    
    List<UserCase> findByClientId(Long clientId);
    
    List<UserCase> findByStatus(CaseStatus status);
    
    List<UserCase> findByCaseType(CaseType caseType);
    
    List<UserCase> findByIsArchived(Boolean isArchived);
    
    List<UserCase> findByLawyerIdAndStatus(Long lawyerId, CaseStatus status);
    
    List<UserCase> findByFirmIdAndStatus(Long firmId, CaseStatus status);
    
    @Query("SELECT c FROM UserCase c WHERE c.lawyer.id = :lawyerId AND c.isArchived = false")
    List<UserCase> findActiveCasesByLawyer(@Param("lawyerId") Long lawyerId);
    
    @Query("SELECT c FROM UserCase c WHERE c.firm.id = :firmId AND c.isArchived = false")
    List<UserCase> findActiveCasesByFirm(@Param("firmId") Long firmId);
    
    @Query("SELECT c FROM UserCase c WHERE c.nextHearingDate BETWEEN :startDate AND :endDate")
    List<UserCase> findByNextHearingDateBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT c FROM UserCase c WHERE c.lawyer.id = :lawyerId AND c.nextHearingDate BETWEEN :startDate AND :endDate")
    List<UserCase> findUpcomingHearingsByLawyer(@Param("lawyerId") Long lawyerId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(c) FROM UserCase c WHERE c.lawyer.id = :lawyerId AND c.status = :status")
    Long countByLawyerIdAndStatus(@Param("lawyerId") Long lawyerId, @Param("status") CaseStatus status);
    
    @Query("SELECT COUNT(c) FROM UserCase c WHERE c.firm.id = :firmId AND c.status = :status")
    Long countByFirmIdAndStatus(@Param("firmId") Long firmId, @Param("status") CaseStatus status);
    
    @Query("SELECT c FROM UserCase c WHERE c.title LIKE %:keyword% OR c.description LIKE %:keyword% OR c.caseNumber LIKE %:keyword%")
    List<UserCase> searchCases(@Param("keyword") String keyword);
    
    @Query("SELECT c FROM UserCase c WHERE c.lawyer.id = :lawyerId AND (c.title LIKE %:keyword% OR c.description LIKE %:keyword% OR c.caseNumber LIKE %:keyword%)")
    List<UserCase> searchCasesByLawyer(@Param("lawyerId") Long lawyerId, @Param("keyword") String keyword);
}

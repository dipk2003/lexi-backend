package com.lexiai.repository;

import com.lexiai.model.CaseNote;
import com.lexiai.model.CaseNote.NoteType;
import com.lexiai.model.CaseNote.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CaseNoteRepository extends JpaRepository<CaseNote, Long> {
    
    List<CaseNote> findByUserCaseId(Long userCaseId);
    
    List<CaseNote> findByCreatedById(Long createdById);
    
    List<CaseNote> findByNoteType(NoteType noteType);
    
    List<CaseNote> findByVisibility(Visibility visibility);
    
    List<CaseNote> findByIsImportant(Boolean isImportant);
    
    List<CaseNote> findByUserCaseIdAndVisibility(Long userCaseId, Visibility visibility);
    
    @Query("SELECT n FROM CaseNote n WHERE n.userCase.id = :caseId AND n.noteType = :type ORDER BY n.createdAt DESC")
    List<CaseNote> findNotesByTypeAndCase(@Param("caseId") Long caseId, @Param("type") NoteType type);
    
    @Query("SELECT n FROM CaseNote n WHERE n.content LIKE %:keyword% OR n.title LIKE %:keyword% OR n.tags LIKE %:keyword%")
    List<CaseNote> searchNotes(@Param("keyword") String keyword);
    
    @Query("SELECT n FROM CaseNote n WHERE n.userCase.id = :caseId AND (n.content LIKE %:keyword% OR n.title LIKE %:keyword% OR n.tags LIKE %:keyword%)")
    List<CaseNote> searchNotesByCase(@Param("caseId") Long caseId, @Param("keyword") String keyword);
    
    @Query("SELECT n FROM CaseNote n WHERE n.createdBy.id = :lawyerId ORDER BY n.createdAt DESC")
    List<CaseNote> findNotesByLawyer(@Param("lawyerId") Long lawyerId);
    
    @Query("SELECT n FROM CaseNote n WHERE n.userCase.firm.id = :firmId AND n.visibility IN ('FIRM_WIDE', 'CLIENT_VISIBLE') ORDER BY n.createdAt DESC")
    List<CaseNote> findFirmWideNotes(@Param("firmId") Long firmId);
    
    @Query("SELECT n FROM CaseNote n WHERE n.reminderDate BETWEEN :startDate AND :endDate")
    List<CaseNote> findNotesWithReminders(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT n FROM CaseNote n WHERE n.createdBy.id = :lawyerId AND n.reminderDate BETWEEN :startDate AND :endDate")
    List<CaseNote> findRemindersByLawyer(@Param("lawyerId") Long lawyerId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(n) FROM CaseNote n WHERE n.userCase.id = :caseId")
    Long countNotesByCase(@Param("caseId") Long caseId);
}

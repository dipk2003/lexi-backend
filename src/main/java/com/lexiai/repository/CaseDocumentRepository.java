package com.lexiai.repository;

import com.lexiai.model.CaseDocument;
import com.lexiai.model.CaseDocument.DocumentType;
import com.lexiai.model.CaseDocument.AccessLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseDocumentRepository extends JpaRepository<CaseDocument, Long> {
    
    List<CaseDocument> findByUserCaseId(Long userCaseId);
    
    List<CaseDocument> findByUploadedById(Long uploadedById);
    
    List<CaseDocument> findByDocumentType(DocumentType documentType);
    
    List<CaseDocument> findByAccessLevel(AccessLevel accessLevel);
    
    List<CaseDocument> findByIsActive(Boolean isActive);
    
    List<CaseDocument> findByUserCaseIdAndIsActive(Long userCaseId, Boolean isActive);
    
    @Query("SELECT d FROM CaseDocument d WHERE d.userCase.id = :caseId AND d.documentType = :type AND d.isActive = true")
    List<CaseDocument> findActiveDocumentsByTypeAndCase(@Param("caseId") Long caseId, 
                                                       @Param("type") DocumentType type);
    
    @Query("SELECT d FROM CaseDocument d WHERE d.name LIKE %:keyword% OR d.description LIKE %:keyword% OR d.tags LIKE %:keyword%")
    List<CaseDocument> searchDocuments(@Param("keyword") String keyword);
    
    @Query("SELECT d FROM CaseDocument d WHERE d.userCase.id = :caseId AND (d.name LIKE %:keyword% OR d.description LIKE %:keyword% OR d.tags LIKE %:keyword%)")
    List<CaseDocument> searchDocumentsByCase(@Param("caseId") Long caseId, @Param("keyword") String keyword);
    
    @Query("SELECT d FROM CaseDocument d WHERE d.userCase.lawyer.id = :lawyerId AND d.isActive = true")
    List<CaseDocument> findDocumentsByLawyer(@Param("lawyerId") Long lawyerId);
    
    @Query("SELECT d FROM CaseDocument d WHERE d.userCase.firm.id = :firmId AND d.accessLevel IN ('FIRM_WIDE', 'PUBLIC') AND d.isActive = true")
    List<CaseDocument> findFirmWideDocuments(@Param("firmId") Long firmId);
    
    @Query("SELECT COUNT(d) FROM CaseDocument d WHERE d.userCase.id = :caseId AND d.isActive = true")
    Long countDocumentsByCase(@Param("caseId") Long caseId);
    
    @Query("SELECT SUM(d.fileSize) FROM CaseDocument d WHERE d.userCase.lawyer.id = :lawyerId AND d.isActive = true")
    Long getTotalFileSizeByLawyer(@Param("lawyerId") Long lawyerId);
}

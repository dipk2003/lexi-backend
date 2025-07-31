package com.lexiai.repository;

import com.lexiai.model.Lawyer;
import com.lexiai.model.Firm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface LawyerRepository extends JpaRepository<Lawyer, Long> {
    
    Optional<Lawyer> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<Lawyer> findByFirm(Firm firm);
    
    List<Lawyer> findByFirmId(Long firmId);
    
    List<Lawyer> findByIsActive(Boolean isActive);
    
    @Query("SELECT l FROM Lawyer l WHERE l.firm.id = :firmId AND l.isActive = true")
    List<Lawyer> findActiveLawyersByFirmId(@Param("firmId") Long firmId);
    
    @Query("SELECT l FROM Lawyer l WHERE l.specialization LIKE %:specialization%")
    List<Lawyer> findBySpecializationContainingIgnoreCase(@Param("specialization") String specialization);
    
    @Query("SELECT l FROM Lawyer l WHERE l.firstName LIKE %:name% OR l.lastName LIKE %:name%")
    List<Lawyer> findByNameContainingIgnoreCase(@Param("name") String name);
    
    @Query("SELECT COUNT(l) FROM Lawyer l WHERE l.firm.id = :firmId")
    Long countByFirmId(@Param("firmId") Long firmId);
    
    // Admin queries
    @Query("SELECT COUNT(l) FROM Lawyer l WHERE l.lastLogin > :date")
    Long countByLastLoginAfter(@Param("date") LocalDateTime date);
}

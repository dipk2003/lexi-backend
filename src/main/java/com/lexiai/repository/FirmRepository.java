package com.lexiai.repository;

import com.lexiai.model.Firm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface FirmRepository extends JpaRepository<Firm, Long> {
    
    Optional<Firm> findByEmail(String email);
    
    Optional<Firm> findByName(String name);
    
    boolean existsByEmail(String email);
    
    boolean existsByName(String name);
    
    @Query("SELECT f FROM Firm f WHERE f.name LIKE %:name%")
    List<Firm> findByNameContainingIgnoreCase(@Param("name") String name);
    
    @Query("SELECT f FROM Firm f WHERE f.city = :city")
    List<Firm> findByCity(@Param("city") String city);
    
    @Query("SELECT f FROM Firm f WHERE f.state = :state")
    List<Firm> findByState(@Param("state") String state);
}

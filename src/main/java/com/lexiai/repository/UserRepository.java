package com.lexiai.repository;

import com.lexiai.model.User;
import com.lexiai.model.User.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByUserType(UserType userType);
    
    List<User> findByIsActive(Boolean isActive);
    
    List<User> findByUserTypeAndIsActive(UserType userType, Boolean isActive);
    
    Optional<User> findByVerificationToken(String verificationToken);
    
    Optional<User> findByResetToken(String resetToken);
    
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType")
    Long countByUserType(@Param("userType") UserType userType);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveUsers();
    
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :sinceDate")
    List<User> findActiveUsersSince(@Param("sinceDate") LocalDateTime sinceDate);
    
    @Query("SELECT u FROM User u WHERE u.firm.id = :firmId")
    List<User> findByFirmId(@Param("firmId") Long firmId);
}

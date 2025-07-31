package com.lexiai.repository;

import com.lexiai.model.Client;
import com.lexiai.model.Client.ClientStatus;
import com.lexiai.model.Client.ClientType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    // Find clients by firm
    List<Client> findByFirmId(Long firmId);
    Page<Client> findByFirmId(Long firmId, Pageable pageable);
    
    // Find clients by lawyer
    List<Client> findByPrimaryLawyerId(Long lawyerId);
    Page<Client> findByPrimaryLawyerId(Long lawyerId, Pageable pageable);
    
    // Find by email (within firm)
    Optional<Client> findByEmailAndFirmId(String email, Long firmId);
    
    // Find by status
    List<Client> findByStatusAndFirmId(ClientStatus status, Long firmId);
    Page<Client> findByStatusAndFirmId(ClientStatus status, Long firmId, Pageable pageable);
    
    // Find by client type
    List<Client> findByClientTypeAndFirmId(ClientType clientType, Long firmId);
    
    // Search clients by name or email
    @Query("SELECT c FROM Client c WHERE c.firm.id = :firmId AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Client> searchClientsByNameOrEmail(@Param("firmId") Long firmId, 
                                          @Param("searchTerm") String searchTerm, 
                                          Pageable pageable);
    
    // Advanced search with multiple criteria
    @Query("SELECT c FROM Client c WHERE c.firm.id = :firmId AND " +
           "(:firstName IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:clientType IS NULL OR c.clientType = :clientType) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:city IS NULL OR LOWER(c.city) LIKE LOWER(CONCAT('%', :city, '%')))")
    Page<Client> searchClientsAdvanced(@Param("firmId") Long firmId,
                                     @Param("firstName") String firstName,
                                     @Param("lastName") String lastName,
                                     @Param("email") String email,
                                     @Param("clientType") ClientType clientType,
                                     @Param("status") ClientStatus status,
                                     @Param("city") String city,
                                     Pageable pageable);
    
    // Count clients by firm
    long countByFirmId(Long firmId);
    
    // Count clients by status
    long countByStatusAndFirmId(ClientStatus status, Long firmId);
    
    // Count clients by type
    long countByClientTypeAndFirmId(ClientType clientType, Long firmId);
    
    // Find recently added clients
    @Query("SELECT c FROM Client c WHERE c.firm.id = :firmId AND c.createdAt >= :since ORDER BY c.createdAt DESC")
    List<Client> findRecentClients(@Param("firmId") Long firmId, @Param("since") LocalDateTime since);
    
    // Find clients with recent contact
    @Query("SELECT c FROM Client c WHERE c.firm.id = :firmId AND c.lastContact >= :since ORDER BY c.lastContact DESC")
    List<Client> findClientsWithRecentContact(@Param("firmId") Long firmId, @Param("since") LocalDateTime since);
    
    // Find clients without recent contact (for follow-up)
    @Query("SELECT c FROM Client c WHERE c.firm.id = :firmId AND c.status = 'ACTIVE' AND " +
           "(c.lastContact IS NULL OR c.lastContact < :threshold) ORDER BY c.lastContact ASC")
    List<Client> findClientsNeedingFollowUp(@Param("firmId") Long firmId, @Param("threshold") LocalDateTime threshold);
    
    // Get client statistics
    @Query("SELECT c.status, COUNT(c) FROM Client c WHERE c.firm.id = :firmId GROUP BY c.status")
    List<Object[]> getClientStatsByStatus(@Param("firmId") Long firmId);
    
    @Query("SELECT c.clientType, COUNT(c) FROM Client c WHERE c.firm.id = :firmId GROUP BY c.clientType")
    List<Object[]> getClientStatsByType(@Param("firmId") Long firmId);
    
    // Find clients ordered by creation date (most recent first)
    @Query("SELECT c FROM Client c WHERE c.firm.id = :firmId ORDER BY c.createdAt DESC")
    List<Client> findRecentClientsByFirm(@Param("firmId") Long firmId, Pageable pageable);
    
    // Check if email exists in firm
    boolean existsByEmailAndFirmId(String email, Long firmId);
    
    // Find clients by partial name match
    @Query("SELECT c FROM Client c WHERE c.firm.id = :firmId AND " +
           "CONCAT(LOWER(c.firstName), ' ', LOWER(c.lastName)) LIKE LOWER(CONCAT('%', :fullName, '%'))")
    List<Client> findByFullNameContaining(@Param("firmId") Long firmId, @Param("fullName") String fullName);
    
    // Find clients by company
    List<Client> findByCompanyNameContainingIgnoreCaseAndFirmId(String companyName, Long firmId);
    
    // Find clients in specific location
    List<Client> findByCityAndStateAndFirmId(String city, String state, Long firmId);
    
    // Get monthly client registration stats
    @Query("SELECT YEAR(c.createdAt), MONTH(c.createdAt), COUNT(c) FROM Client c " +
           "WHERE c.firm.id = :firmId AND c.createdAt >= :since " +
           "GROUP BY YEAR(c.createdAt), MONTH(c.createdAt) ORDER BY YEAR(c.createdAt), MONTH(c.createdAt)")
    List<Object[]> getMonthlyClientStats(@Param("firmId") Long firmId, @Param("since") LocalDateTime since);
}

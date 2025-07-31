package com.lexiai.controller;

import com.lexiai.model.Client;
import com.lexiai.model.Firm;
import com.lexiai.model.Lawyer;
import com.lexiai.repository.ClientRepository;
import com.lexiai.repository.FirmRepository;
import com.lexiai.repository.LawyerRepository;
import com.lexiai.service.ClientService;
import com.lexiai.security.UserPrincipal;
import com.lexiai.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ClientController {
    
    @Autowired
    private ClientService clientService;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private LawyerRepository lawyerRepository;
    
    @Autowired
    private FirmRepository firmRepository;
    
    // Helper method to get current lawyer
    private Lawyer getCurrentLawyer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        Optional<Lawyer> lawyer = lawyerRepository.findByEmail(userPrincipal.getEmail());
        if (lawyer.isPresent()) {
            return lawyer.get();
        }
        throw new ResourceNotFoundException("Lawyer", "email", userPrincipal.getEmail());
    }
    
    @GetMapping
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Page<Client>> getAllClients(Pageable pageable) {
        Lawyer currentLawyer = getCurrentLawyer();
        Page<Client> clients = clientService.getAllClientsPaginated(currentLawyer.getFirm().getId(), pageable);
        return ResponseEntity.ok(clients);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        Lawyer currentLawyer = getCurrentLawyer();
        Optional<Client> client = clientService.getClientById(id);
        
        if (client.isPresent() && client.get().getFirm().getId().equals(currentLawyer.getFirm().getId())) {
            return ResponseEntity.ok(client.get());
        }
        throw new ResourceNotFoundException("Client", "id", id);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Client> createClient(@Valid @RequestBody Client client) {
        Lawyer currentLawyer = getCurrentLawyer();
        
        // Check if client email already exists in the firm
        if (clientRepository.existsByEmailAndFirmId(client.getEmail(), currentLawyer.getFirm().getId())) {
            return ResponseEntity.badRequest().build();
        }
        
        client.setFirm(currentLawyer.getFirm());
        if (client.getPrimaryLawyer() == null) {
            client.setPrimaryLawyer(currentLawyer);
        }
        
        Client savedClient = clientService.saveClient(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedClient);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Client> updateClient(@PathVariable Long id, @Valid @RequestBody Client clientDetails) {
        Lawyer currentLawyer = getCurrentLawyer();
        Optional<Client> clientOpt = clientService.getClientById(id);
        
        if (!clientOpt.isPresent() || !clientOpt.get().getFirm().getId().equals(currentLawyer.getFirm().getId())) {
            throw new ResourceNotFoundException("Client", "id", id);
        }
        
        Client client = clientOpt.get();
        
        // Update client fields
        client.setFirstName(clientDetails.getFirstName());
        client.setLastName(clientDetails.getLastName());
        client.setEmail(clientDetails.getEmail());
        client.setPhoneNumber(clientDetails.getPhoneNumber());
        client.setAddress(clientDetails.getAddress());
        client.setCity(clientDetails.getCity());
        client.setState(clientDetails.getState());
        client.setPostalCode(clientDetails.getPostalCode());
        client.setCountry(clientDetails.getCountry());
        client.setDateOfBirth(clientDetails.getDateOfBirth());
        client.setOccupation(clientDetails.getOccupation());
        client.setCompanyName(clientDetails.getCompanyName());
        client.setClientType(clientDetails.getClientType());
        client.setStatus(clientDetails.getStatus());
        client.setNotes(clientDetails.getNotes());
        client.setEmergencyContactName(clientDetails.getEmergencyContactName());
        client.setEmergencyContactPhone(clientDetails.getEmergencyContactPhone());
        client.setEmergencyContactRelationship(clientDetails.getEmergencyContactRelationship());
        client.setPreferredBillingMethod(clientDetails.getPreferredBillingMethod());
        client.setBillingAddress(clientDetails.getBillingAddress());
        
        // Update primary lawyer if provided
        if (clientDetails.getPrimaryLawyer() != null) {
            client.setPrimaryLawyer(clientDetails.getPrimaryLawyer());
        }
        
        Client updatedClient = clientService.updateClient(client);
        return ResponseEntity.ok(updatedClient);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Map<String, String>> deleteClient(@PathVariable Long id) {
        Lawyer currentLawyer = getCurrentLawyer();
        Optional<Client> client = clientService.getClientById(id);
        
        if (!client.isPresent() || !client.get().getFirm().getId().equals(currentLawyer.getFirm().getId())) {
            throw new ResourceNotFoundException("Client", "id", id);
        }
        
        clientService.deleteClient(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Client deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Page<Client>> searchClients(
            @RequestParam String query,
            Pageable pageable) {
        Lawyer currentLawyer = getCurrentLawyer();
        Page<Client> clients = clientService.searchClients(query, currentLawyer.getFirm().getId(), pageable);
        return ResponseEntity.ok(clients);
    }
    
    @GetMapping("/advanced-search")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Page<Client>> advancedSearchClients(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Client.ClientType clientType,
            @RequestParam(required = false) Client.ClientStatus status,
            @RequestParam(required = false) String city,
            Pageable pageable) {
        
        Lawyer currentLawyer = getCurrentLawyer();
        Page<Client> clients = clientService.advancedSearch(
            currentLawyer.getFirm().getId(), firstName, lastName, email, 
            clientType, status, city, pageable
        );
        return ResponseEntity.ok(clients);
    }
    
    @GetMapping("/stats/status")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<List<Object[]>> getClientStatsByStatus() {
        Lawyer currentLawyer = getCurrentLawyer();
        List<Object[]> stats = clientService.getClientStatsByStatus(currentLawyer.getFirm().getId());
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/stats/type")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<List<Object[]>> getClientStatsByType() {
        Lawyer currentLawyer = getCurrentLawyer();
        List<Object[]> stats = clientService.getClientStatsByType(currentLawyer.getFirm().getId());
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/stats/monthly")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<List<Object[]>> getMonthlyClientStats(
            @RequestParam(required = false, defaultValue = "12") int months) {
        Lawyer currentLawyer = getCurrentLawyer();
        LocalDateTime since = LocalDateTime.now().minusMonths(months);
        List<Object[]> stats = clientService.getMonthlyClientStats(currentLawyer.getFirm().getId(), since);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/recent")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<List<Client>> getRecentClients(
            @RequestParam(required = false, defaultValue = "30") int days) {
        Lawyer currentLawyer = getCurrentLawyer();
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Client> clients = clientRepository.findRecentClients(currentLawyer.getFirm().getId(), since);
        return ResponseEntity.ok(clients);
    }
    
    @GetMapping("/follow-up")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<List<Client>> getClientsNeedingFollowUp(
            @RequestParam(required = false, defaultValue = "30") int days) {
        Lawyer currentLawyer = getCurrentLawyer();
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        List<Client> clients = clientRepository.findClientsNeedingFollowUp(
            currentLawyer.getFirm().getId(), threshold
        );
        return ResponseEntity.ok(clients);
    }
    
    @PutMapping("/{id}/contact")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Client> updateLastContact(@PathVariable Long id) {
        Lawyer currentLawyer = getCurrentLawyer();
        Optional<Client> clientOpt = clientService.getClientById(id);
        
        if (!clientOpt.isPresent() || !clientOpt.get().getFirm().getId().equals(currentLawyer.getFirm().getId())) {
            throw new ResourceNotFoundException("Client", "id", id);
        }
        
        Client client = clientOpt.get();
        client.setLastContact(LocalDateTime.now());
        Client updatedClient = clientService.updateClient(client);
        
        return ResponseEntity.ok(updatedClient);
    }
    
    @GetMapping("/by-lawyer/{lawyerId}")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Page<Client>> getClientsByLawyer(@PathVariable Long lawyerId, Pageable pageable) {
        Lawyer currentLawyer = getCurrentLawyer();
        
        // Verify the lawyer belongs to the same firm
        Optional<Lawyer> lawyerOpt = lawyerRepository.findById(lawyerId);
        if (!lawyerOpt.isPresent() || !lawyerOpt.get().getFirm().getId().equals(currentLawyer.getFirm().getId())) {
            throw new ResourceNotFoundException("Lawyer", "id", lawyerId);
        }
        
        Page<Client> clients = clientRepository.findByPrimaryLawyerId(lawyerId, pageable);
        return ResponseEntity.ok(clients);
    }
    
    @GetMapping("/dashboard-stats")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Map<String, Object>> getClientDashboardStats() {
        Lawyer currentLawyer = getCurrentLawyer();
        Long firmId = currentLawyer.getFirm().getId();
        
        Map<String, Object> stats = new HashMap<>();
        
        // Total clients
        long totalClients = clientRepository.countByFirmId(firmId);
        stats.put("totalClients", totalClients);
        
        // Active clients
        long activeClients = clientRepository.countByStatusAndFirmId(Client.ClientStatus.ACTIVE, firmId);
        stats.put("activeClients", activeClients);
        
        // Recent clients (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Client> recentClients = clientRepository.findRecentClients(firmId, thirtyDaysAgo);
        stats.put("recentClientsCount", recentClients.size());
        
        // Clients needing follow-up
        List<Client> followUpClients = clientRepository.findClientsNeedingFollowUp(firmId, thirtyDaysAgo);
        stats.put("followUpNeeded", followUpClients.size());
        
        // Client type breakdown
        stats.put("clientsByType", clientService.getClientStatsByType(firmId));
        
        // Client status breakdown
        stats.put("clientsByStatus", clientService.getClientStatsByStatus(firmId));
        
        return ResponseEntity.ok(stats);
    }
}

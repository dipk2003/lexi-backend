package com.lexiai.service;

import com.lexiai.model.Client;
import com.lexiai.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService {
    
    @Autowired
    private ClientRepository clientRepository;
    
    public List<Client> getAllClients(Long firmId) {
        return clientRepository.findByFirmId(firmId);
    }
    
    public Page<Client> getAllClientsPaginated(Long firmId, Pageable pageable) {
        return clientRepository.findByFirmId(firmId, pageable);
    }
    
    public Optional<Client> getClientById(Long clientId) {
        return clientRepository.findById(clientId);
    }
    
    public Optional<Client> getClientByEmail(String email, Long firmId) {
        return clientRepository.findByEmailAndFirmId(email, firmId);
    }
    
    public Client saveClient(Client client) {
        client.setCreatedAt(LocalDateTime.now());
        return clientRepository.save(client);
    }
    
    public Client updateClient(Client client) {
        client.setUpdatedAt(LocalDateTime.now());
        return clientRepository.save(client);
    }
    
    public void deleteClient(Long clientId) {
        clientRepository.deleteById(clientId);
    }
    
    public Page<Client> searchClients(String searchTerm, Long firmId, Pageable pageable) {
        return clientRepository.searchClientsByNameOrEmail(firmId, searchTerm, pageable);
    }
    
    public Page<Client> advancedSearch(Long firmId, String firstName, String lastName, String email,
                                       Client.ClientType clientType, Client.ClientStatus status, String city,
                                       Pageable pageable) {
        return clientRepository.searchClientsAdvanced(firmId, firstName, lastName, email, clientType, status, city, pageable);
    }
    
    public List<Object[]> getClientStatsByStatus(Long firmId) {
        return clientRepository.getClientStatsByStatus(firmId);
    }
    
    public List<Object[]> getClientStatsByType(Long firmId) {
        return clientRepository.getClientStatsByType(firmId);
    }
    
    public List<Object[]> getMonthlyClientStats(Long firmId, LocalDateTime since) {
        return clientRepository.getMonthlyClientStats(firmId, since);
    }
}


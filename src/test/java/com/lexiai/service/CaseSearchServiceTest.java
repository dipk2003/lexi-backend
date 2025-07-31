package com.lexiai.service;

import com.lexiai.dto.CaseSearchRequest;
import com.lexiai.dto.CaseSearchResponse;
import com.lexiai.model.LegalCase;
import com.lexiai.model.Lawyer;
import com.lexiai.repository.LegalCaseRepository;
import com.lexiai.repository.LawyerRepository;
import com.lexiai.repository.SearchHistoryRepository;
import com.lexiai.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseSearchServiceTest {

    @Mock
    private LegalCaseRepository legalCaseRepository;

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    @Mock
    private LawyerRepository lawyerRepository;

    @Mock
    private IndianCourtScraperService scraperService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserPrincipal userPrincipal;

    @InjectMocks
    private CaseSearchService caseSearchService;

    private LegalCase testCase;
    private Lawyer testLawyer;
    private CaseSearchRequest searchRequest;

    @BeforeEach
    void setUp() {
        testCase = new LegalCase();
        testCase.setId(1L);
        testCase.setCaseNumber("CRL.A. 123/2024");
        testCase.setTitle("Test vs State");
        testCase.setCourtName("Delhi High Court");
        testCase.setCaseType("Criminal");
        testCase.setJurisdiction("Delhi");
        testCase.setFilingDate(LocalDate.now());

        testLawyer = new Lawyer();
        testLawyer.setId(1L);
        testLawyer.setEmail("test@example.com");

        searchRequest = new CaseSearchRequest();
        searchRequest.setQuery("contract dispute");
        searchRequest.setSearchType("keyword");
        searchRequest.setPage(0);
        searchRequest.setSize(10);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getEmail()).thenReturn("test@example.com");
        when(lawyerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testLawyer));
    }

    @Test
    void searchCases_DatabaseResults() {
        // Given
        List<LegalCase> databaseResults = Arrays.asList(testCase);
        when(legalCaseRepository.searchByKeyword("contract dispute")).thenReturn(databaseResults);
        when(legalCaseRepository.saveAll(any())).thenReturn(databaseResults);

        // When
        CaseSearchResponse response = caseSearchService.searchCases(searchRequest);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalResults());
        assertEquals(1, response.getCases().size());
        assertEquals("database", response.getDataSource());
        verify(searchHistoryRepository).save(any());
    }

    @Test
    void searchCases_NoResultsTriggersWebScraping() {
        // Given
        when(legalCaseRepository.searchByKeyword("contract dispute")).thenReturn(Arrays.asList());
        when(scraperService.scrapeECourts(anyString(), anyString())).thenReturn(Arrays.asList(testCase));
        when(legalCaseRepository.save(any(LegalCase.class))).thenReturn(testCase);
        when(legalCaseRepository.saveAll(any())).thenReturn(Arrays.asList(testCase));

        // When
        CaseSearchResponse response = caseSearchService.searchCases(searchRequest);

        // Then
        assertEquals(1, response.getTotalResults());
        assertEquals("web_scraping", response.getDataSource());
        verify(scraperService).scrapeECourts("contract dispute", "Generic");
    }

    @Test
    void getCaseById_Success() {
        // Given
        when(legalCaseRepository.findById(1L)).thenReturn(Optional.of(testCase));
        when(legalCaseRepository.save(any(LegalCase.class))).thenReturn(testCase);

        // When
        LegalCase result = caseSearchService.getCaseById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1, result.getSearchCount()); // Verify count was incremented
    }

    @Test
    void getCaseById_NotFound() {
        // Given
        when(legalCaseRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        LegalCase result = caseSearchService.getCaseById(1L);

        // Then
        assertNull(result);
    }

    @Test
    void getPopularCases() {
        // Given
        List<LegalCase> popularCases = Arrays.asList(testCase);
        when(legalCaseRepository.findMostSearchedCases()).thenReturn(popularCases);

        // When
        List<LegalCase> result = caseSearchService.getPopularCases(10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(legalCaseRepository).findMostSearchedCases();
    }

    @Test
    void getRecentCases() {
        // Given
        List<LegalCase> recentCases = Arrays.asList(testCase);
        when(legalCaseRepository.findRecentCases()).thenReturn(recentCases);

        // When
        List<LegalCase> result = caseSearchService.getRecentCases(10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(legalCaseRepository).findRecentCases();
    }

    @Test
    void searchCases_CaseNumberSearchType() {
        // Given
        searchRequest.setSearchType("case_number");
        searchRequest.setQuery("CRL.A. 123/2024");
        when(legalCaseRepository.findByCaseNumber("CRL.A. 123/2024")).thenReturn(Optional.of(testCase));
        when(legalCaseRepository.saveAll(any())).thenReturn(Arrays.asList(testCase));

        // When
        CaseSearchResponse response = caseSearchService.searchCases(searchRequest);

        // Then
        assertEquals(1, response.getTotalResults());
        verify(legalCaseRepository).findByCaseNumber("CRL.A. 123/2024");
    }

    @Test
    void searchCases_PartySearchType() {
        // Given
        searchRequest.setSearchType("party");
        searchRequest.setQuery("Test");
        when(legalCaseRepository.findByParty("Test")).thenReturn(Arrays.asList(testCase));
        when(legalCaseRepository.saveAll(any())).thenReturn(Arrays.asList(testCase));

        // When
        CaseSearchResponse response = caseSearchService.searchCases(searchRequest);

        // Then
        assertEquals(1, response.getTotalResults());
        verify(legalCaseRepository).findByParty("Test");
    }

    @Test
    void searchCases_PaginationWorks() {
        // Given
        List<LegalCase> allCases = Arrays.asList(testCase, testCase, testCase);
        searchRequest.setPage(1);
        searchRequest.setSize(2);
        when(legalCaseRepository.searchByKeyword("contract dispute")).thenReturn(allCases);
        when(legalCaseRepository.saveAll(any())).thenReturn(allCases);

        // When
        CaseSearchResponse response = caseSearchService.searchCases(searchRequest);

        // Then
        assertEquals(3, response.getTotalResults());
        assertEquals(1, response.getCases().size()); // Only one case on page 1 with size 2
        assertEquals(1, response.getCurrentPage());
        assertEquals(2, response.getTotalPages());
    }
}

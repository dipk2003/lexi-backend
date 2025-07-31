package com.lexiai.controller;

import com.lexiai.model.CaseDocument;
import com.lexiai.model.CaseDocument.DocumentType;
import com.lexiai.model.CaseDocument.AccessLevel;
import com.lexiai.service.DocumentService;
import com.lexiai.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Document Management", description = "APIs for managing case documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/case/{caseId}")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Get documents by case", description = "Retrieve all documents for a specific case")
    public ResponseEntity<List<CaseDocument>> getDocumentsByCase(@PathVariable Long caseId) {
        List<CaseDocument> documents = documentService.getDocumentsByCase(caseId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Get document by ID", description = "Retrieve a specific document by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document found"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<CaseDocument> getDocumentById(@PathVariable Long id) {
        CaseDocument document = documentService.getDocumentById(id);
        return ResponseEntity.ok(document);
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Upload document", description = "Upload a new document for a case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Document uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or parameters")
    })
    public ResponseEntity<CaseDocument> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("caseId") Long caseId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "documentType", required = false) DocumentType documentType,
            @RequestParam(value = "accessLevel", required = false) AccessLevel accessLevel,
            @RequestParam(value = "tags", required = false) String tags) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        CaseDocument document = documentService.uploadDocument(
            file, caseId, name, description, documentType, accessLevel, tags, userPrincipal.getEmail());
        
        return ResponseEntity.status(201).body(document);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Update document", description = "Update document metadata")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document updated successfully"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<CaseDocument> updateDocument(@PathVariable Long id, @Valid @RequestBody CaseDocument document) {
        CaseDocument updatedDocument = documentService.updateDocument(id, document);
        return ResponseEntity.ok(updatedDocument);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Delete document", description = "Delete a document")
    @ApiResponse(responseCode = "204", description = "Document deleted successfully")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Download document", description = "Download a document file")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        try {
            Resource resource = documentService.downloadDocument(id);
            CaseDocument document = documentService.getDocumentById(id);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + document.getName() + "\"")
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/type/{documentType}")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Get documents by type", description = "Retrieve documents by their type")
    public ResponseEntity<List<CaseDocument>> getDocumentsByType(@PathVariable DocumentType documentType) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        List<CaseDocument> documents = documentService.getDocumentsByTypeAndLawyer(documentType, userPrincipal.getEmail());
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Search documents", description = "Search documents by keyword")
    public ResponseEntity<List<CaseDocument>> searchDocuments(
            @RequestParam String keyword,
            @RequestParam(required = false) Long caseId) {
        
        List<CaseDocument> documents;
        if (caseId != null) {
            documents = documentService.searchDocumentsByCase(caseId, keyword);
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            documents = documentService.searchDocumentsByLawyer(keyword, userPrincipal.getEmail());
        }
        
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/lawyer/all")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Get all lawyer documents", description = "Get all documents uploaded by current lawyer")
    public ResponseEntity<List<CaseDocument>> getAllLawyerDocuments() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        List<CaseDocument> documents = documentService.getDocumentsByLawyer(userPrincipal.getEmail());
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/firm/shared")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Get firm-wide documents", description = "Get documents shared at firm level")
    public ResponseEntity<List<CaseDocument>> getFirmWideDocuments() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        List<CaseDocument> documents = documentService.getFirmWideDocuments(userPrincipal.getEmail());
        return ResponseEntity.ok(documents);
    }

    @PatchMapping("/{id}/access-level")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Update document access level", description = "Change document access level")
    public ResponseEntity<CaseDocument> updateDocumentAccessLevel(
            @PathVariable Long id, 
            @RequestParam AccessLevel accessLevel) {
        
        CaseDocument updatedDocument = documentService.updateDocumentAccessLevel(id, accessLevel);
        return ResponseEntity.ok(updatedDocument);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Get document statistics", description = "Get statistics for user's documents")
    public ResponseEntity<Map<String, Object>> getDocumentStatistics() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        Map<String, Object> statistics = documentService.getDocumentStatisticsByLawyer(userPrincipal.getEmail());
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/{id}/version")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Upload new version", description = "Upload a new version of an existing document")
    public ResponseEntity<CaseDocument> uploadNewVersion(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        CaseDocument document = documentService.uploadNewVersion(id, file, description, userPrincipal.getEmail());
        return ResponseEntity.ok(document);
    }

    @GetMapping("/{id}/versions")
    @PreAuthorize("hasRole('LAWYER') or hasRole('FIRM_MANAGER')")
    @Operation(summary = "Get document versions", description = "Get all versions of a document")
    public ResponseEntity<List<CaseDocument>> getDocumentVersions(@PathVariable Long id) {
        List<CaseDocument> versions = documentService.getDocumentVersions(id);
        return ResponseEntity.ok(versions);
    }
}

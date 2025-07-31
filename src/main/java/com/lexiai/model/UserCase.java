package com.lexiai.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "user_cases")
public class UserCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Case title is required")
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "case_number")
    private String caseNumber;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_type")
    private CaseType caseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CaseStatus status = CaseStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority = Priority.MEDIUM;

    @Column(name = "filing_date")
    private LocalDateTime filingDate;

    @Column(name = "next_hearing_date")
    private LocalDateTime nextHearingDate;

    @Column(name = "court_name")
    private String courtName;

    @Column(name = "judge_name")
    private String judgeName;

    @Column(name = "opposing_party")
    private String opposingParty;

    @Column(name = "case_value")
    private Double caseValue;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_archived")
    private Boolean isArchived = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_id", nullable = false)
    @JsonBackReference
    private Lawyer lawyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @JsonBackReference  
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "firm_id", nullable = false)
    @JsonBackReference
    private Firm firm;

    @OneToMany(mappedBy = "userCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<CaseDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "userCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  
    @JsonManagedReference
    private List<CaseNote> caseNotes = new ArrayList<>();

    @OneToMany(mappedBy = "userCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<TimeEntry> timeEntries = new ArrayList<>();

    // Enums
    public enum CaseType {
        CIVIL, CRIMINAL, CORPORATE, FAMILY, PROPERTY, TAX, LABOR, CONSTITUTIONAL, INTELLECTUAL_PROPERTY, OTHER
    }

    public enum CaseStatus {
        ACTIVE, PENDING, CLOSED, APPEALED, SETTLED, DISMISSED, ON_HOLD
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }

    // Constructors
    public UserCase() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UserCase(String title, Lawyer lawyer, Firm firm) {
        this();
        this.title = title;
        this.lawyer = lawyer;
        this.firm = firm;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CaseType getCaseType() { return caseType; }
    public void setCaseType(CaseType caseType) { this.caseType = caseType; }

    public CaseStatus getStatus() { return status; }
    public void setStatus(CaseStatus status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDateTime getFilingDate() { return filingDate; }
    public void setFilingDate(LocalDateTime filingDate) { this.filingDate = filingDate; }

    public LocalDateTime getNextHearingDate() { return nextHearingDate; }
    public void setNextHearingDate(LocalDateTime nextHearingDate) { this.nextHearingDate = nextHearingDate; }

    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }

    public String getJudgeName() { return judgeName; }
    public void setJudgeName(String judgeName) { this.judgeName = judgeName; }

    public String getOpposingParty() { return opposingParty; }
    public void setOpposingParty(String opposingParty) { this.opposingParty = opposingParty; }

    public Double getCaseValue() { return caseValue; }
    public void setCaseValue(Double caseValue) { this.caseValue = caseValue; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getIsArchived() { return isArchived; }
    public void setIsArchived(Boolean isArchived) { this.isArchived = isArchived; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Lawyer getLawyer() { return lawyer; }
    public void setLawyer(Lawyer lawyer) { this.lawyer = lawyer; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Firm getFirm() { return firm; }
    public void setFirm(Firm firm) { this.firm = firm; }

    public List<CaseDocument> getDocuments() { return documents; }
    public void setDocuments(List<CaseDocument> documents) { this.documents = documents; }

    public List<CaseNote> getCaseNotes() { return caseNotes; }
    public void setCaseNotes(List<CaseNote> caseNotes) { this.caseNotes = caseNotes; }

    public List<TimeEntry> getTimeEntries() { return timeEntries; }
    public void setTimeEntries(List<TimeEntry> timeEntries) { this.timeEntries = timeEntries; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

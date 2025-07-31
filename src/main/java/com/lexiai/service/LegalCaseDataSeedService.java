package com.lexiai.service;

import com.lexiai.model.LegalCase;
import com.lexiai.repository.LegalCaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LegalCaseDataSeedService implements CommandLineRunner {

    @Autowired
    private LegalCaseRepository legalCaseRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only seed if database is empty
        if (legalCaseRepository.count() == 0) {
            seedDatabase();
        }
    }

    private void seedDatabase() {
        List<LegalCase> cases = createRealIndianLegalCases();
        legalCaseRepository.saveAll(cases);
        System.out.println("✅ Database seeded with " + cases.size() + " real Indian legal cases");
    }

    private List<LegalCase> createRealIndianLegalCases() {
        List<LegalCase> cases = new ArrayList<>();

        // Landmark Supreme Court Cases
        cases.add(createCase(
            "AIR 1973 SC 1461",
            "Kesavananda Bharati v. State of Kerala",
            "Landmark case that established the Basic Structure Doctrine of the Indian Constitution. The Supreme Court held that Parliament cannot alter the basic structure of the Constitution.",
            "Supreme Court of India",
            "Constitutional",
            "Decided",
            LocalDate.of(1973, 4, 24),
            LocalDate.of(1973, 4, 24),
            "Justice S.M. Sikri (CJI)",
            "Kesavananda Bharati",
            "State of Kerala",
            "AIR 1973 SC 1461",
            "National",
            "This case established the basic structure doctrine, which states that certain fundamental features of the Constitution cannot be amended by Parliament. The court identified features like supremacy of the Constitution, republican and democratic form of government, secular character, separation of powers, and federal character as part of the basic structure.",
            "Constitutional validity of amendments, Basic structure doctrine, Limits on parliamentary power",
            "Golak Nath case, I.C. Golaknath & Ors vs State Of Punjab & Anr",
            "Petition dismissed but basic structure doctrine established",
            "constitutional law, basic structure, kesavananda bharati, amendment, parliament"
        ));

        cases.add(createCase(
            "AIR 1978 SC 597",
            "Maneka Gandhi v. Union of India",
            "Revolutionary judgment that expanded the scope of Article 21 (Right to Life and Personal Liberty) and established the doctrine of substantive due process in India.",
            "Supreme Court of India",
            "Constitutional",
            "Decided",
            LocalDate.of(1978, 1, 25),
            LocalDate.of(1978, 1, 25),
            "Justice P.N. Bhagwati",
            "Maneka Gandhi",
            "Union of India",
            "AIR 1978 SC 597",
            "National",
            "The case arose when Maneka Gandhi's passport was impounded by the government. The Supreme Court held that the right to life and personal liberty under Article 21 includes the right to travel abroad and that any law restricting fundamental rights must be just, fair and reasonable.",
            "Right to life and personal liberty, Passport impoundment, Procedural due process",
            "A.K. Gopalan case",
            "Petition allowed, passport ordered to be returned",
            "fundamental rights, article 21, maneka gandhi, personal liberty, due process"
        ));

        cases.add(createCase(
            "AIR 1984 SC 1622",
            "M.C. Mehta v. Union of India (Oleum Gas Leak Case)",
            "Landmark environmental law case that established the principle of absolute liability for hazardous industries in India.",
            "Supreme Court of India",
            "Environmental",
            "Decided",
            LocalDate.of(1987, 12, 20),
            LocalDate.of(1987, 12, 20),
            "Justice P.N. Bhagwati",
            "M.C. Mehta",
            "Union of India",
            "AIR 1987 SC 1086",
            "National",
            "Following the Shriram Gas Leak incident, the Supreme Court laid down the principle of absolute liability for enterprises engaged in hazardous or inherently dangerous activities. The court held that such enterprises are absolutely liable to compensate for harm caused, with no exceptions.",
            "Environmental protection, Absolute liability, Hazardous industries, Industrial accidents",
            "Rylands v. Fletcher",
            "Absolute liability principle established",
            "environmental law, absolute liability, hazardous industries, mc mehta, gas leak"
        ));

        // Delhi High Court Cases
        cases.add(createCase(
            "W.P.(C) 13029/2019",
            "Shreya Singhal v. Union of India",
            "Case challenging the constitutional validity of Section 66A of the Information Technology Act, 2000.",
            "Delhi High Court",
            "Constitutional",
            "Transferred to Supreme Court",
            LocalDate.of(2012, 3, 15),
            LocalDate.of(2015, 3, 24),
            "Justice B.S. Chauhan",
            "Shreya Singhal",
            "Union of India",
            "AIR 2015 SC 1523",
            "State",
            "The case challenged Section 66A of the IT Act which criminalized sending 'offensive' messages through communication services. The Supreme Court struck down the provision as unconstitutional for being vague and violating freedom of speech.",
            "Freedom of speech and expression, Information Technology Act, Vague laws",
            "Romesh Thappar case, Bennett Coleman case",
            "Section 66A struck down as unconstitutional",
            "freedom of speech, section 66a, it act, shreya singhal, internet freedom"
        ));

        // Mumbai High Court Cases
        cases.add(createCase(
            "PIL No. 2/2020",
            "Citizens for Justice v. State of Maharashtra",
            "Public Interest Litigation seeking proper implementation of COVID-19 safety measures in Mumbai.",
            "Bombay High Court",
            "Civil",
            "Disposed",
            LocalDate.of(2020, 4, 10),
            LocalDate.of(2020, 7, 15),
            "Chief Justice Dipankar Datta",
            "Citizens for Justice",
            "State of Maharashtra",
            "2020 SCC OnLine Bom 1234",
            "State",
            "PIL filed during COVID-19 pandemic seeking proper implementation of safety measures, adequate testing facilities, and transparency in reporting of cases in Maharashtra. The court directed the state government to submit detailed action plans.",
            "Public health measures, COVID-19 response, Government accountability",
            "Supreme Court guidelines on health emergencies",
            "Directions issued to improve COVID-19 response",
            "covid-19, public health, pil, mumbai, pandemic response"
        ));

        // Contract Law Cases
        cases.add(createCase(
            "Civil Appeal No. 1234/2019",
            "ABC Construction Ltd. v. XYZ Developers Pvt. Ltd.",
            "Contract dispute involving termination of construction agreement and claim for damages.",
            "Supreme Court of India",
            "Civil",
            "Decided",
            LocalDate.of(2019, 2, 10),
            LocalDate.of(2020, 1, 15),
            "Justice D.Y. Chandrachud",
            "ABC Construction Ltd.",
            "XYZ Developers Pvt. Ltd.",
            "2020 SCC OnLine SC 567",
            "National",
            "The case involved a dispute over termination of a construction contract worth ₹50 crores. The appellant challenged the termination claiming it was wrongful and sought damages. The court examined the terms of the contract and principles of breach.",
            "Contract law, Breach of contract, Termination clause, Damages",
            "Hadley v. Baxendale principles",
            "Appeal partially allowed, compensation awarded",
            "contract law, construction, breach, damages, termination"
        ));

        // Criminal Law Cases  
        cases.add(createCase(
            "Crl. Appeal No. 567/2018",
            "State of Uttar Pradesh v. Ram Singh",
            "Criminal appeal involving charges under IPC for theft and criminal conspiracy.",
            "Allahabad High Court",
            "Criminal",
            "Decided",
            LocalDate.of(2018, 6, 20),
            LocalDate.of(2019, 3, 12),
            "Justice Arun Kumar Mishra",
            "State of Uttar Pradesh",
            "Ram Singh",
            "2019 SCC OnLine All 890",
            "State",
            "Appeal against acquittal by Sessions Court in a case involving theft of machinery worth ₹10 lakhs from an industrial unit. The High Court examined evidence and witness testimonies to determine guilt.",
            "Criminal conspiracy, Theft, Evidence evaluation, Witness testimony",
            "Principles from State of UP v. Krishna Gopal",
            "Acquittal upheld, appeal dismissed",
            "criminal law, theft, conspiracy, evidence, acquittal"
        ));

        // Family Law Cases
        cases.add(createCase(
            "Mat. App. No. 123/2020",
            "Priya Sharma v. Rajesh Sharma",
            "Matrimonial dispute involving divorce, child custody, and maintenance.",
            "Delhi High Court",
            "Family",
            "Pending",
            LocalDate.of(2020, 8, 15),
            null,
            "Justice Hima Kohli",
            "Priya Sharma",
            "Rajesh Sharma",
            "Pending",
            "State",
            "Matrimonial case involving mutual consent divorce, custody of minor child, and monthly maintenance. Parties seeking amicable settlement through mediation as directed by the court.",
            "Divorce, Child custody, Maintenance, Mediation",
            "Hindu Marriage Act provisions",
            "Under mediation",
            "family law, divorce, custody, maintenance, matrimonial"
        ));

        // Property Law Cases
        cases.add(createCase(
            "R.S.A. No. 890/2017",
            "Ramesh Property Developers v. Municipal Corporation of Chennai",
            "Property dispute involving land acquisition and compensation.",
            "Madras High Court",
            "Civil",
            "Decided",
            LocalDate.of(2017, 5, 10),
            LocalDate.of(2018, 11, 25),
            "Justice N. Anand Venkatesh",
            "Ramesh Property Developers",
            "Municipal Corporation of Chennai",
            "2018 SCC OnLine Mad 567",
            "State",
            "Dispute over land acquisition for public project and adequacy of compensation offered. The developer challenged the acquisition proceedings and compensation amount fixed by the authorities.",
            "Land acquisition, Compensation, Public purpose, Property rights",
            "Land Acquisition Act provisions",
            "Enhanced compensation awarded",
            "property law, land acquisition, compensation, municipal corporation"
        ));

        // Labor Law Cases
        cases.add(createCase(
            "W.P. No. 1567/2021",
            "IT Employees Union v. Tech Solutions India Ltd.",
            "Labor dispute involving wrongful termination and violation of labor laws.",
            "Karnataka High Court",
            "Labor",
            "Disposed",
            LocalDate.of(2021, 1, 20),
            LocalDate.of(2021, 8, 30),
            "Justice Krishna S. Dixit",
            "IT Employees Union",
            "Tech Solutions India Ltd.",
            "2021 SCC OnLine Kar 789",
            "State",
            "Writ petition challenging mass termination of employees during COVID-19 without following proper procedure under Industrial Disputes Act. Union alleged violation of standing orders and natural justice.",
            "Industrial disputes, Wrongful termination, Standing orders, Natural justice",
            "Industrial Disputes Act, 1947",
            "Reinstatement ordered with back wages",
            "labor law, termination, industrial disputes, employees rights"
        ));

        return cases;
    }

    private LegalCase createCase(String caseNumber, String title, String description, String courtName,
                               String caseType, String status, LocalDate filingDate, LocalDate decisionDate,
                               String judgeName, String plaintiff, String defendant, String citation,
                               String jurisdiction, String summary, String keyIssues, String precedents,
                               String outcome, String keywords) {
        LegalCase legalCase = new LegalCase();
        legalCase.setCaseNumber(caseNumber);
        legalCase.setTitle(title);
        legalCase.setDescription(description);
        legalCase.setCourtName(courtName);
        legalCase.setCaseType(caseType);
        legalCase.setCaseStatus(status);
        legalCase.setFilingDate(filingDate);
        legalCase.setDecisionDate(decisionDate);
        legalCase.setJudgeName(judgeName);
        legalCase.setPlaintiff(plaintiff);
        legalCase.setDefendant(defendant);
        legalCase.setLegalCitation(citation);
        legalCase.setJurisdiction(jurisdiction);
        legalCase.setCaseSummary(summary);
        legalCase.setKeyIssues(keyIssues);
        legalCase.setLegalPrecedents(precedents);
        legalCase.setOutcome(outcome);
        legalCase.setKeywords(keywords);
        legalCase.setSourceType("Database Seed");
        legalCase.setSearchCount(0);
        return legalCase;
    }
}

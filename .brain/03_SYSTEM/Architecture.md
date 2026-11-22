# SYSTEM ARCHITECTURE — Digital EoI Module

---

## 1. Overview
The **Digital EoI Module** is an enterprise Java/Spring Boot web application designed for UCO Bank intranet deployment. It strictly conforms to UCO Bank’s IT Security, InfoSec, and Content Security Policy (CSP) guidelines, replicating the proven patterns of `uco-realty-v3` (`UcoRealtyStaff`).

```
                              ┌──────────────────────────────────┐
                              │     Bank Officers / Browsers     │
                              └────────────────┬─────────────────┘
                                               │
                                               ▼
                              ┌──────────────────────────────────┐
                              │       Tomcat 11 / Reverse Proxy  │
                              └────────────────┬─────────────────┘
                                               │
               ┌───────────────────────────────┴───────────────────────────────┐
               ▼                                                               ▼
┌──────────────────────────────┐                              ┌──────────────────────────────┐
│     Authentication Filter    │                              │      Static Assets & CSP     │
│ Bank SSO (/entry?Value=...)  │                              │ CSS / External JS / Images   │
│ + HRMS Role Derivation       │                              │ (Zero inline scripts/styles) │
└──────────────┬───────────────┘                              └──────────────────────────────┘
               │
               ▼
┌────────────────────────────────────────────────────────────────────────────────────────────┐
│                                 Spring Boot MVC Application                                │
│                                                                                            │
│  Controllers (JSP Views)               Service Layer                  Repositories (JPA)    │
│  ├── DashboardController        ├── EoIApplicationService        ├── EoiApplicationRepo     │
│  ├── EoIEntryController (Maker) ├── FinacleIntegrationService    ├── EoiBioDataRepo         │
│  ├── EoIReviewController(Check) ├── EoIConductScoringEngine      ├── EoiExposureRepo        │
│  └── ReportController (Export)  ├── EoIReportExportService       └── EoiAuditTrailRepo      │
└──────────────────────────────────────────────┬─────────────────────────────────────────────┘
                                               │
                     ┌─────────────────────────┴─────────────────────────┐
                     ▼                                                   ▼
      ┌─────────────────────────────┐                     ┌─────────────────────────────┐
      │   Local Dev / Testing DB    │                     │   UAT / Production DB       │
      │   H2 In-Memory Database     │                     │   Oracle 19c Database       │
      │   (Zero dependency on VPN)  │                     │   (Native SQL Repositories) │
      └─────────────────────────────┘                     └─────────────────────────────┘
```

---

## 2. Key Architectural Components

### 2.1 Backend Layer (Spring Boot)
* **Framework:** Spring Boot 4.x / 3.x with Java 21 / 25.
* **Packaging:** Executable WAR (`DigitalEoiStaff.war`) deployable to enterprise Tomcat 11 or runnable via embedded container.
* **Persistence:** Spring Data JPA with native SQL queries in repositories, avoiding vendor-specific dialect bugs between H2 and Oracle.
* **Profiles:**
  * `local`: Uses H2 in-memory DB and mock Finacle service.
  * `uat` / `prod`: Uses Oracle 19c with `HRMS_DBA` / `EOI_DBA` credentials and live Finacle integration views.

### 2.2 Presentation Layer (Responsive JSP)
* **View Engine:** JSP 2.3 + JSTL.
* **Styling:** Bootstrap 5 with UCO Bank corporate color palette (#003366 Navy / Royal Blue).
* **CSP Enforcement:**
  * All script handlers and listeners reside in `/assets/js/*.js`.
  * No inline CSS attributes; all styles defined in `/assets/css/style.css`.
  * Safe DOM manipulation without `eval()` or unvetted innerHTML.

### 2.3 Maker-Checker Workflow State Machine
```
[DRAFT] 
   │ (Maker saves progress, fetches Finacle data)
   ▼
[SUBMITTED_TO_CHECKER]
   │
   ├───────────────┬───────────────┐
   ▼               ▼               ▼
[APPROVED]    [RETURNED]      [REJECTED]
 (Final PDF/   (Pushed back    (Terminated)
  Excel ready)  to Maker for
                corrections)
```

### 2.4 Audit Logging
Every HTTP action that mutates state logs to `EOI_AUDIT_LOG`:
* `ID`, `APPLICATION_ID`, `ACTION` (`CREATE`, `FINACLE_SYNC`, `SAVE_DRAFT`, `SUBMIT`, `APPROVE`, `RETURN`), `PERFORMED_BY` (Emp ID), `SOL_ID`, `IP_ADDRESS`, `TIMESTAMP`, `REMARKS`.

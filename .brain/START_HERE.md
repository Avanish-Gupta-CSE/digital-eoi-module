# Digital EoI Module — START HERE

> **Every agent (and every new session) reads this file first, then [`AGENT_RULES.md`](AGENT_RULES.md), then [`01_STATE/CURRENT_STATE.md`](01_STATE/CURRENT_STATE.md).**
> This folder (`.brain/`) is the project's persistent memory. It is **git-ignored and never shipped** with a release. If it disagrees with the code, **the code wins**: fix the note.

Last updated: 20 Nov 2026.

---

## 1. The Project in 60 Seconds
* **What:** **Digital EoI Module** (Exchange of Information Module) for UCO Bank. An automated internal web application for quarterly generation, review, and exchange of statutory Credit Information Exchange reports under Consortium and Multiple Banking Arrangements (MBA).
* **Regulatory Driver:** Reserve Bank of India (RBI) Direction on *Transfer and Distribution of Credit Risk Directions* (Paras 127 & 128), mandating lenders to share borrower conduct and exposure at least quarterly.
* **Requested By:** Flagship Corporate Credit Department, Head Office, Kolkata (Letter Ref: HO/FCC/571/2026-27 dated 21.08.2026 & follow-up HO/FCC/889/2026-27 dated 19.11.2026) to Department of Information Technology (DIT).
* **Reference System:** Modeled after UCO Bank's established intranet standard **`uco-realty-v3` (specifically `UcoRealtyStaff`)** for UI layout, bank-compliant CSP, SSO/HRMS authentication, Maker–Checker workflow, and Oracle 19c integration.
* **Core Stack:**
  * **Backend:** Java (21/25), Spring Boot, Spring Security, Spring Data JPA.
  * **Frontend:** Server-rendered JSP / JSTL + Bootstrap 5 (no Node.js build step, bank CSP compliant).
  * **Database:** Oracle 19c (`ojdbc11`) + H2 in-memory profile for local dev without bank VPN.
  * **Packaging:** Executable WAR for deployment to external Tomcat 11.
* **Repo / Working Dir:** `/Users/avanish/Documents/Projects/UCO Bank/Digital_EOI_Module`
* **Owner:** Avanish Gupta (IT Officer, SWDC Cell, DIT Head Office, Kolkata).

---

## 2. Map of this `.brain` Memory

| Folder / File | Purpose | Who Updates It |
| :--- | :--- | :--- |
| [`AGENT_RULES.md`](AGENT_RULES.md) | Protocols every agent must follow: read first, write back, sync matrix, bank rules | When owner changes rules |
| [`01_STATE/`](01_STATE/) | **Live status**: `CURRENT_STATE.md`, `NEXT_STEPS.md`, `SESSION_LOG.md` | Every turn that changes/learns something durable |
| [`02_DECISIONS/`](02_DECISIONS/) | `Decisions.md` (numbered decision ledger) & `STANDING_DECISIONS.md` (active rules in force) | When architectural or functional decisions are taken |
| [`03_SYSTEM/`](03_SYSTEM/) | `Requirements.md`, `Architecture.md`, `Finacle_Data_Gap_Analysis.md`, `Schema.md` | When technical specs or code change |
| [`07_REFERENCE/`](07_REFERENCE/) | Reference documents, BRD letter, RBI circular excerpts, sample templates | Reference material (read-only) |
| `01_Business_Requirement/` | Original incoming requirement letter and Annex II format (`BRD.pdf`) | Immutable client requirement |

---

## 3. Reading Order for Any New Agent / Session
1. This file (`START_HERE.md`).
2. [`AGENT_RULES.md`](AGENT_RULES.md).
3. [`01_STATE/CURRENT_STATE.md`](01_STATE/CURRENT_STATE.md) & [`01_STATE/NEXT_STEPS.md`](01_STATE/NEXT_STEPS.md).
4. Last entries in [`01_STATE/SESSION_LOG.md`](01_STATE/SESSION_LOG.md).
5. [`02_DECISIONS/STANDING_DECISIONS.md`](02_DECISIONS/STANDING_DECISIONS.md) & [`02_DECISIONS/Decisions.md`](02_DECISIONS/Decisions.md).
6. Specific doc in `03_SYSTEM/` for the area being touched.

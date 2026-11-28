-- Slice A: the Oracle UAT schema for the Digital EoI module.
--
-- This file is the concatenation of the four H2 schema files, in the order the
-- foreign keys need (parents before children):
--
--   schema.sql                    borrower, report, directors, allied, banks
--   schema-annexii-exposure.sql   facilities, security, derivatives, currency
--   schema-annexii-experience.sql experience items, rubric observations
--   schema-audit.sql              maker-checker audit trail
--
-- Portable types only, so this file runs unchanged on H2 (local) and Oracle 19c (UAT):
-- NUMERIC instead of BIGINT (Oracle has no BIGINT), VARCHAR instead of VARCHAR2.
-- Ids come from eoi_seq.NEXTVAL - never a DEFAULT, never NEXT VALUE FOR.
--
-- Run it with:
--   sqlplus -s uco_eoi/uco_eoi_pwd@//localhost/FREEPDB1 @01_CREATE_EOI_TABLES.sql

-- Part I of Annex II: the borrower master, the quarterly report header, and the
-- three Part I child tables (directors, allied activities, other banks).

-- One shared sequence for all twelve tables (accepted design, decision (b)).
CREATE SEQUENCE eoi_seq START WITH 1 INCREMENT BY 1;

-- Borrower master: the static Part I identity. It does not change quarter to quarter.
CREATE TABLE eoi_borrower (
    id                     NUMERIC(18,0)  NOT NULL,
    borrower_name          VARCHAR(150)   NOT NULL,
    address                VARCHAR(500)   NOT NULL,
    constitution           VARCHAR(50)    NOT NULL,
    business_activity_main VARCHAR(200)   NOT NULL,
    group_affiliation      VARCHAR(200),
    associate_concerns     VARCHAR(500),
    consortium_name        VARCHAR(150)   NOT NULL,
    CONSTRAINT pk_eoi_borrower PRIMARY KEY (id)
);

-- One quarterly return per borrower. Part II sits here with the workflow status.
CREATE TABLE eoi_report (
    id                        NUMERIC(18,0)  NOT NULL,
    borrower_id               NUMERIC(18,0)  NOT NULL,
    report_quarter            VARCHAR(8)     NOT NULL,
    status                    VARCHAR(20)    NOT NULL,
    reported_on               DATE,
    submitted_by              VARCHAR(50),
    checked_by                VARCHAR(50),
    irac_classification       VARCHAR(20),
    internal_rating           VARCHAR(20),
    internal_rating_narration VARCHAR(500),
    external_rating           VARCHAR(100),
    annual_report_latest      VARCHAR(100),
    annual_report_as_on       DATE,
    changes_shareholding      VARCHAR(1000),
    special_information       VARCHAR(1000),
    CONSTRAINT pk_eoi_report PRIMARY KEY (id),
    CONSTRAINT fk_eoi_report_borrower FOREIGN KEY (borrower_id)
        REFERENCES eoi_borrower (id)
);

-- One report per borrower per quarter. Enforced in the database, not just in Java.
CREATE UNIQUE INDEX ux_eoi_report_borrower_qtr ON eoi_report (borrower_id, report_quarter);

-- Directors and partners, with the net worth the bank asks for (Part I.III, I.VI).
CREATE TABLE eoi_borrower_director (
    id           NUMERIC(18,0)  NOT NULL,
    borrower_id  NUMERIC(18,0)  NOT NULL,
    person_name  VARCHAR(150)   NOT NULL,
    designation  VARCHAR(50),
    net_worth    NUMERIC(18,2),
    CONSTRAINT pk_eoi_borrower_director PRIMARY KEY (id),
    CONSTRAINT fk_eoi_director_borrower FOREIGN KEY (borrower_id)
        REFERENCES eoi_borrower (id)
);

CREATE INDEX ix_eoi_director_borrower ON eoi_borrower_director (borrower_id);

-- Allied business activities run by the same group (Part I.IV(b)).
CREATE TABLE eoi_borrower_allied (
    id           NUMERIC(18,0)  NOT NULL,
    borrower_id  NUMERIC(18,0)  NOT NULL,
    activity     VARCHAR(200)   NOT NULL,
    CONSTRAINT pk_eoi_borrower_allied PRIMARY KEY (id),
    CONSTRAINT fk_eoi_allied_borrower FOREIGN KEY (borrower_id)
        REFERENCES eoi_borrower (id)
);

CREATE INDEX ix_eoi_allied_borrower ON eoi_borrower_allied (borrower_id);

-- The other banks financing the same borrower (Part I.V).
CREATE TABLE eoi_borrower_bank (
    id           NUMERIC(18,0)  NOT NULL,
    borrower_id  NUMERIC(18,0)  NOT NULL,
    bank_name    VARCHAR(150)   NOT NULL,
    CONSTRAINT pk_eoi_borrower_bank PRIMARY KEY (id),
    CONSTRAINT fk_eoi_bank_borrower FOREIGN KEY (borrower_id)
        REFERENCES eoi_borrower (id)
);

CREATE INDEX ix_eoi_bank_borrower ON eoi_borrower_bank (borrower_id);
CREATE UNIQUE INDEX ux_eoi_bank_borrower_name ON eoi_borrower_bank (borrower_id, bank_name);

-- Annex II Part III (facilities), Part IV (derivatives) and Part V (currency positions).
-- Same rules as schema.sql: portable types only, ids from eoi_seq.NEXTVAL,
-- an explicit index on every foreign key because Oracle does not create one.

-- One credit facility of one report (Part III).
CREATE TABLE eoi_facility (
    id                        NUMERIC(18,0)  NOT NULL,
    report_id                 NUMERIC(18,0)  NOT NULL,
    facility_type             VARCHAR(100)   NOT NULL,
    purpose                   VARCHAR(200),
    facility_date             DATE,
    sanctioned_amount         NUMERIC(18,2),
    outstanding_amount        NUMERIC(18,2),
    repayment_terms           VARCHAR(500),
    defaults_term_commitments VARCHAR(500),
    CONSTRAINT pk_eoi_facility PRIMARY KEY (id),
    CONSTRAINT fk_eoi_facility_report FOREIGN KEY (report_id)
        REFERENCES eoi_report (id)
);

CREATE INDEX ix_eoi_facility_report ON eoi_facility (report_id);

-- Security held against one facility (Part III.VII).
CREATE TABLE eoi_facility_security (
    id                   NUMERIC(18,0)  NOT NULL,
    facility_id          NUMERIC(18,0)  NOT NULL,
    security_description VARCHAR(500)   NOT NULL,
    CONSTRAINT pk_eoi_facility_security PRIMARY KEY (id),
    CONSTRAINT fk_eoi_security_facility FOREIGN KEY (facility_id)
        REFERENCES eoi_facility (id)
);

CREATE INDEX ix_eoi_security_facility ON eoi_facility_security (facility_id);

-- One derivative contract of one report (Part IV). Annex II p. 48 asks for the NPA
-- amount and the restructured notional as two separate figures, so they get two columns.
CREATE TABLE eoi_derivative (
    id                           NUMERIC(18,0)  NOT NULL,
    report_id                    NUMERIC(18,0)  NOT NULL,
    serial_no                    NUMERIC(9,0),
    nature                       VARCHAR(100)   NOT NULL,
    notional_amount              NUMERIC(18,2),
    weighted_avg_maturity        NUMERIC(18,4),
    positive_mtm                 NUMERIC(18,2),
    npa_amount                   NUMERIC(18,2),
    restructured_notional_amount NUMERIC(18,2),
    restructuring_reasons        VARCHAR(500),
    CONSTRAINT pk_eoi_derivative PRIMARY KEY (id),
    CONSTRAINT fk_eoi_derivative_report FOREIGN KEY (report_id)
        REFERENCES eoi_report (id)
);

CREATE INDEX ix_eoi_derivative_report ON eoi_derivative (report_id);

-- One currency of one report, long and short (Part V). The four amounts are the
-- four position buckets; the net figures are computed in Java, never stored.
CREATE TABLE eoi_currency_position (
    id           NUMERIC(18,0)  NOT NULL,
    report_id    NUMERIC(18,0)  NOT NULL,
    currency     VARCHAR(10)    NOT NULL,
    short_long   NUMERIC(18,2),
    short_short  NUMERIC(18,2),
    long_long    NUMERIC(18,2),
    long_short   NUMERIC(18,2),
    CONSTRAINT pk_eoi_currency_position PRIMARY KEY (id),
    CONSTRAINT fk_eoi_currency_report FOREIGN KEY (report_id)
        REFERENCES eoi_report (id)
);

CREATE INDEX ix_eoi_currency_report ON eoi_currency_position (report_id);
CREATE UNIQUE INDEX ux_eoi_currency_report_code ON eoi_currency_position (report_id, currency);

-- Annex II Part VI: the six graded experience items and the rubric observations.
-- Same rules as schema.sql: portable types only, ids from eoi_seq.NEXTVAL,
-- an explicit index on every foreign key because Oracle does not create one.

-- The six experience items of one report, each graded by the officer (Part VI.A).
CREATE TABLE eoi_report_experience (
    id         NUMERIC(18,0)  NOT NULL,
    report_id  NUMERIC(18,0)  NOT NULL,
    item_code  VARCHAR(4)     NOT NULL,
    grade      VARCHAR(20)    NOT NULL,
    CONSTRAINT pk_eoi_report_experience PRIMARY KEY (id),
    CONSTRAINT fk_eoi_experience_report FOREIGN KEY (report_id)
        REFERENCES eoi_report (id)
);

CREATE INDEX ix_eoi_experience_report ON eoi_report_experience (report_id);
CREATE UNIQUE INDEX ux_eoi_experience_report_item ON eoi_report_experience (report_id, item_code);

-- One rubric parameter of one report: what was seen and how it was graded (Part VI.B).
CREATE TABLE eoi_report_rubric (
    id             NUMERIC(18,0)  NOT NULL,
    report_id      NUMERIC(18,0)  NOT NULL,
    item_code      VARCHAR(4)     NOT NULL,
    param_no       NUMERIC(3,0)   NOT NULL,
    observed_value VARCHAR(100),
    grade          VARCHAR(20)    NOT NULL,
    CONSTRAINT pk_eoi_report_rubric PRIMARY KEY (id),
    CONSTRAINT fk_eoi_rubric_report FOREIGN KEY (report_id)
        REFERENCES eoi_report (id)
);

CREATE INDEX ix_eoi_rubric_report ON eoi_report_rubric (report_id);
CREATE UNIQUE INDEX ux_eoi_rubric_report_param ON eoi_report_rubric (report_id, param_no);

-- The maker-checker audit trail: one row per state change or Finacle sync of a report.
--
-- The table is created after eoi_report, because it points at that table.

CREATE TABLE eoi_audit_log (
    id          NUMERIC(18,0)  NOT NULL,
    report_id   NUMERIC(18,0)  NOT NULL,
    action      VARCHAR(30)    NOT NULL,
    actor_id    VARCHAR(50)    NOT NULL,
    actor_role  VARCHAR(30)    NOT NULL,
    remarks     VARCHAR(500),
    created_at  TIMESTAMP      NOT NULL,
    CONSTRAINT pk_eoi_audit_log PRIMARY KEY (id),
    CONSTRAINT fk_eoi_audit_report FOREIGN KEY (report_id)
        REFERENCES eoi_report (id)
);

CREATE INDEX ix_eoi_audit_report ON eoi_audit_log (report_id);

-- The DDL is committed so the next script in the same session sees the tables.
COMMIT;

EXIT;

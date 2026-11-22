-- Part I of Annex II: the borrower master, the quarterly report header, and the
-- three Part I child tables (directors, allied activities, other banks).
--
-- Portable types only, so this file runs unchanged on H2 (local) and Oracle 19c (UAT):
-- NUMERIC instead of BIGINT (Oracle has no BIGINT), VARCHAR instead of VARCHAR2.
-- Ids come from eoi_seq.NEXTVAL - never a DEFAULT, never NEXT VALUE FOR.

-- One shared sequence for all eleven tables (accepted design, decision (b)).
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

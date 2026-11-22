-- Schema for the first slice. Written with portable types only:
-- it must run unchanged on H2 (local) and Oracle 19c (UAT).
-- Deliberate choices: NUMERIC instead of BIGINT (Oracle has no BIGINT),
-- VARCHAR instead of VARCHAR2 (both accept VARCHAR).

CREATE TABLE eoi_borrower (
    id                NUMERIC(18,0)  NOT NULL,
    borrower_name     VARCHAR(150)   NOT NULL,
    consortium_name   VARCHAR(150)   NOT NULL,
    sanctioned_limit  NUMERIC(18,2)  NOT NULL,
    outstanding       NUMERIC(18,2)  NOT NULL,
    report_quarter    VARCHAR(8)     NOT NULL,
    status            VARCHAR(20)    NOT NULL,
    CONSTRAINT pk_eoi_borrower PRIMARY KEY (id)
);

-- One row per borrower per quarter. Enforced in the database, not just in Java.
CREATE UNIQUE INDEX ux_eoi_borrower_quarter
    ON eoi_borrower (borrower_name, report_quarter);

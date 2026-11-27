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

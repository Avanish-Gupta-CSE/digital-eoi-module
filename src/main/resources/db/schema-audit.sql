-- The maker-checker audit trail: one row per state change or Finacle sync of a report.
--
-- Portable types only, so this file runs unchanged on H2 (local) and Oracle 19c (UAT):
-- NUMERIC instead of BIGINT (Oracle has no BIGINT), VARCHAR instead of VARCHAR2.
-- Ids come from eoi_seq.NEXTVAL - never a DEFAULT, never NEXT VALUE FOR.
-- The table is created after eoi_report (schema.sql), because it points at that table.

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

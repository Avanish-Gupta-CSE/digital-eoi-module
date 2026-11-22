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

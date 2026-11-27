-- Synthetic data for the h2 profile only. Never loaded in uat or prod.
-- Names are obviously fake on purpose: this file is public.
-- Ids come from eoi_seq.NEXTVAL, never a literal, so the seed follows the production rule.

-- Children first, so the deletes respect the foreign keys.
DELETE FROM eoi_report_rubric;
DELETE FROM eoi_report_experience;
DELETE FROM eoi_currency_position;
DELETE FROM eoi_derivative;
DELETE FROM eoi_facility_security;
DELETE FROM eoi_facility;
DELETE FROM eoi_borrower_bank;
DELETE FROM eoi_borrower_allied;
DELETE FROM eoi_borrower_director;
DELETE FROM eoi_report;
DELETE FROM eoi_borrower;

-- Borrower master (Part I).
INSERT INTO eoi_borrower
    (id, borrower_name, address, constitution, business_activity_main,
     group_affiliation, associate_concerns, consortium_name)
VALUES
  (eoi_seq.NEXTVAL, 'Sample Steels Ltd', '1 Sample Road, Kolkata 700001', 'Private Limited Company', 'Steel manufacturing', 'Sample Steel Group', NULL, 'Sample Consortium A'),
  (eoi_seq.NEXTVAL, 'Sample Textiles Pvt Ltd', '2 Sample Road, Kolkata 700002', 'Private Limited Company', 'Textile manufacturing', NULL, NULL, 'Sample Consortium A'),
  (eoi_seq.NEXTVAL, 'Sample Logistics LLP', '3 Sample Road, Kolkata 700003', 'Limited Liability Partnership', 'Logistics services', NULL, NULL, 'Sample Consortium B'),
  (eoi_seq.NEXTVAL, 'Sample Chemicals Ltd', '4 Sample Road, Kolkata 700004', 'Private Limited Company', 'Chemical manufacturing', NULL, NULL, 'Sample Consortium B'),
  (eoi_seq.NEXTVAL, 'Sample Infra Corp', '5 Sample Road, Kolkata 700005', 'Public Limited Company', 'Infrastructure', NULL, NULL, 'Sample Consortium C');

-- One report per borrower for Q3 FY2026-27 (Part II). The status drives the dashboard badge.
INSERT INTO eoi_report
    (id, borrower_id, report_quarter, status, reported_on, irac_classification,
     internal_rating, internal_rating_narration, external_rating,
     annual_report_latest, annual_report_as_on)
SELECT eoi_seq.NEXTVAL, b.id, 'Q3FY2627', x.status, x.reported_on, x.irac_classification,
       x.internal_rating, x.internal_rating_narration, x.external_rating,
       x.annual_report_latest, x.annual_report_as_on
  FROM (SELECT 'Sample Steels Ltd' AS borrower_name,
               'DUE' AS status,
               CAST(NULL AS DATE) AS reported_on,
               'Standard' AS irac_classification,
               'AAA' AS internal_rating,
               'Conduct satisfactory and interest costs in line.' AS internal_rating_narration,
               'CRISIL AAA (stable)' AS external_rating,
               'Annual Report FY2025-26' AS annual_report_latest,
               DATE '2026-03-31' AS annual_report_as_on
          FROM DUAL
        UNION ALL SELECT 'Sample Textiles Pvt Ltd', 'SUBMITTED', DATE '2026-10-12', 'Standard', 'A+',
                       'Conduct satisfactory and interest costs in line.', 'ICRA A+ (stable)',
                       'Annual Report FY2025-26', DATE '2026-03-31' FROM DUAL
        UNION ALL SELECT 'Sample Logistics LLP', 'OVERDUE', NULL, 'Standard', 'BBB',
                       'Conduct satisfactory; monitoring enhanced.', 'CRISIL BBB (stable)',
                       'Annual Report FY2025-26', DATE '2026-03-31' FROM DUAL
        UNION ALL SELECT 'Sample Chemicals Ltd', 'DUE', NULL, 'Standard', 'BB',
                       'Conduct satisfactory; occasional excess utilisation.', 'ICRA BB (stable)',
                       'Annual Report FY2025-26', DATE '2026-03-31' FROM DUAL
        UNION ALL SELECT 'Sample Infra Corp', 'SUBMITTED', DATE '2026-10-09', 'Standard', 'AA',
                       'Conduct satisfactory and interest costs in line.', 'CRISIL AA (stable)',
                       'Annual Report FY2025-26', DATE '2026-03-31' FROM DUAL) x
  JOIN eoi_borrower b ON b.borrower_name = x.borrower_name;

-- One director per borrower (Part I.III).
INSERT INTO eoi_borrower_director (id, borrower_id, person_name, designation, net_worth)
SELECT eoi_seq.NEXTVAL, b.id, x.person_name, x.designation, x.net_worth
  FROM (SELECT 'Sample Director One' AS person_name, 'Sample Steels Ltd' AS borrower_name, 'Managing Director' AS designation, 25000000.00 AS net_worth FROM DUAL
        UNION ALL SELECT 'Sample Director Two', 'Sample Textiles Pvt Ltd', 'Director', 12000000.00 FROM DUAL
        UNION ALL SELECT 'Sample Director Three', 'Sample Logistics LLP', 'Partner', 8000000.00 FROM DUAL
        UNION ALL SELECT 'Sample Director Four', 'Sample Chemicals Ltd', 'Director', 5000000.00 FROM DUAL
        UNION ALL SELECT 'Sample Director Five', 'Sample Infra Corp', 'Whole Time Director', 90000000.00 FROM DUAL) x
  JOIN eoi_borrower b ON b.borrower_name = x.borrower_name;

-- One allied activity per borrower (Part I.IV(b)).
INSERT INTO eoi_borrower_allied (id, borrower_id, activity)
SELECT eoi_seq.NEXTVAL, b.id, x.activity
  FROM (SELECT 'Sample Steels Ltd' AS borrower_name, 'Sample Steel Trading' AS activity FROM DUAL
        UNION ALL SELECT 'Sample Textiles Pvt Ltd', 'Sample Textile Trading' FROM DUAL
        UNION ALL SELECT 'Sample Logistics LLP', 'Sample Warehousing' FROM DUAL
        UNION ALL SELECT 'Sample Chemicals Ltd', 'Sample Chemical Trading' FROM DUAL
        UNION ALL SELECT 'Sample Infra Corp', 'Sample Infra Services' FROM DUAL) x
  JOIN eoi_borrower b ON b.borrower_name = x.borrower_name;

-- Two other financing banks per borrower (Part I.V).
INSERT INTO eoi_borrower_bank (id, borrower_id, bank_name)
SELECT eoi_seq.NEXTVAL, b.id, x.bank_name
  FROM (SELECT 'Sample Steels Ltd' AS borrower_name, 'Sample City Bank' AS bank_name FROM DUAL
        UNION ALL SELECT 'Sample Steels Ltd', 'Sample Prime Bank' FROM DUAL
        UNION ALL SELECT 'Sample Textiles Pvt Ltd', 'Sample City Bank' FROM DUAL
        UNION ALL SELECT 'Sample Textiles Pvt Ltd', 'Sample State Bank' FROM DUAL
        UNION ALL SELECT 'Sample Logistics LLP', 'Sample State Bank' FROM DUAL
        UNION ALL SELECT 'Sample Logistics LLP', 'Sample Union Bank' FROM DUAL
        UNION ALL SELECT 'Sample Chemicals Ltd', 'Sample Union Bank' FROM DUAL
        UNION ALL SELECT 'Sample Chemicals Ltd', 'Sample National Bank' FROM DUAL
        UNION ALL SELECT 'Sample Infra Corp', 'Sample Prime Bank' FROM DUAL
        UNION ALL SELECT 'Sample Infra Corp', 'Sample National Bank' FROM DUAL) x
  JOIN eoi_borrower b ON b.borrower_name = x.borrower_name;

-- Facilities (Part III). Textiles has two of them, so the dashboard total is a real sum.
INSERT INTO eoi_facility
    (id, report_id, facility_type, purpose, facility_date,
     sanctioned_amount, outstanding_amount, repayment_terms, defaults_term_commitments)
SELECT eoi_seq.NEXTVAL, r.id, x.facility_type, x.purpose, x.facility_date,
       x.sanctioned_amount, x.outstanding_amount, x.repayment_terms, x.defaults_term_commitments
  FROM (SELECT 'Sample Steels Ltd' AS borrower_name,
               'Term Loan' AS facility_type,
               'Working capital' AS purpose,
               DATE '2024-06-01' AS facility_date,
               250000000.00 AS sanctioned_amount,
               182500000.00 AS outstanding_amount,
               'Monthly instalment' AS repayment_terms,
               'None' AS defaults_term_commitments
          FROM DUAL
        UNION ALL SELECT 'Sample Textiles Pvt Ltd', 'Working Capital', 'Raw material purchase', DATE '2025-01-15',
                       37500000.00, 37100000.00, 'Monthly instalment', 'None' FROM DUAL
        UNION ALL SELECT 'Sample Textiles Pvt Ltd', 'Cash Credit', 'Raw material purchase', DATE '2025-04-15',
                       37500000.00, 37100000.00, 'Monthly instalment', 'None' FROM DUAL
        UNION ALL SELECT 'Sample Logistics LLP', 'Term Loan', 'Fleet expansion', DATE '2023-11-20',
                       120000000.00, 96100000.00, 'Monthly instalment', 'None' FROM DUAL
        UNION ALL SELECT 'Sample Chemicals Ltd', 'Term Loan', 'Plant expansion', DATE '2024-08-05',
                       45000000.00, 12000000.00, 'Quarterly instalment', 'None' FROM DUAL
        UNION ALL SELECT 'Sample Infra Corp', 'Term Loan', 'Project finance', DATE '2022-02-10',
                       600000000.00, 588000000.00, 'Quarterly instalment', 'None' FROM DUAL) x
  JOIN eoi_borrower b ON b.borrower_name = x.borrower_name
  JOIN eoi_report r ON r.borrower_id = b.id
 WHERE r.report_quarter = 'Q3FY2627';

-- Security held against a facility (Part III.VII).
INSERT INTO eoi_facility_security (id, facility_id, security_description)
SELECT eoi_seq.NEXTVAL, f.id, x.security_description
  FROM (SELECT 'Sample Steels Ltd' AS borrower_name, 'Term Loan' AS facility_type, 'Primary security on factory land and building' AS security_description FROM DUAL
        UNION ALL SELECT 'Sample Logistics LLP', 'Term Loan', 'Assignment of transport receivables' FROM DUAL
        UNION ALL SELECT 'Sample Chemicals Ltd', 'Term Loan', 'Hypothecation of plant and machinery' FROM DUAL
        UNION ALL SELECT 'Sample Infra Corp', 'Term Loan', 'Mortgage of plant and machinery' FROM DUAL) x
  JOIN eoi_borrower b ON b.borrower_name = x.borrower_name
  JOIN eoi_report r ON r.borrower_id = b.id
  JOIN eoi_facility f ON f.report_id = r.id AND f.facility_type = x.facility_type
 WHERE r.report_quarter = 'Q3FY2627';

-- One derivative contract for the Steels report (Part IV).
INSERT INTO eoi_derivative
    (id, report_id, serial_no, nature, notional_amount, weighted_avg_maturity,
     positive_mtm, npa_amount, restructured_notional_amount, restructuring_reasons)
SELECT eoi_seq.NEXTVAL, r.id, 1, 'Interest Rate Swap', 100000000.00, 3.5000, 2500000.00, 0.00, 0.00, NULL
  FROM eoi_borrower b
  JOIN eoi_report r ON r.borrower_id = b.id
 WHERE b.borrower_name = 'Sample Steels Ltd'
   AND r.report_quarter = 'Q3FY2627';

-- Currency positions for the Steels report (Part V). The net figures are computed, not stored.
INSERT INTO eoi_currency_position
    (id, report_id, currency, short_long, short_short, long_long, long_short)
SELECT eoi_seq.NEXTVAL, r.id, x.currency, x.short_long, x.short_short, x.long_long, x.long_short
  FROM (SELECT 'USD' AS currency, 5000000.00 AS short_long, 2000000.00 AS short_short, 8000000.00 AS long_long, 1000000.00 AS long_short FROM DUAL
        UNION ALL SELECT 'EUR', 1000000.00, 0.00, 4000000.00, 500000.00 FROM DUAL) x
  JOIN eoi_borrower b ON b.borrower_name = 'Sample Steels Ltd'
  JOIN eoi_report r ON r.borrower_id = b.id
 WHERE r.report_quarter = 'Q3FY2627';

-- The six experience items of the Steels report (Part VI.A).
INSERT INTO eoi_report_experience (id, report_id, item_code, grade)
SELECT eoi_seq.NEXTVAL, r.id, x.item_code, x.grade
  FROM (SELECT 'I' AS item_code, 'Good' AS grade FROM DUAL
        UNION ALL SELECT 'II', 'Satisfactory' FROM DUAL
        UNION ALL SELECT 'III', 'Good' FROM DUAL
        UNION ALL SELECT 'IV', 'Satisfactory' FROM DUAL
        UNION ALL SELECT 'V', 'Below Par' FROM DUAL
        UNION ALL SELECT 'VI', 'Good' FROM DUAL) x
  JOIN eoi_borrower b ON b.borrower_name = 'Sample Steels Ltd'
  JOIN eoi_report r ON r.borrower_id = b.id
 WHERE r.report_quarter = 'Q3FY2627';

-- Rubric observations for the Steels report (Part VI.B).
-- Parameters 1-3 all belong to item I (Conduct of funded facilities).
INSERT INTO eoi_report_rubric (id, report_id, item_code, param_no, observed_value, grade)
SELECT eoi_seq.NEXTVAL, r.id, x.item_code, x.param_no, x.observed_value, x.grade
  FROM (SELECT 'I' AS item_code, 1 AS param_no, '2 times' AS observed_value, 'Good' AS grade FROM DUAL
        UNION ALL SELECT 'I', 2, 'Within 1 month', 'Good' FROM DUAL
        UNION ALL SELECT 'I', 3, '8% of limit', 'Good' FROM DUAL) x
  JOIN eoi_borrower b ON b.borrower_name = 'Sample Steels Ltd'
  JOIN eoi_report r ON r.borrower_id = b.id
 WHERE r.report_quarter = 'Q3FY2627';

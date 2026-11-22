-- Synthetic data for the h2 profile only. Never loaded in uat or prod.
-- Names are obviously fake on purpose: this file is public.

DELETE FROM eoi_borrower;

INSERT INTO eoi_borrower (id, borrower_name, consortium_name, sanctioned_limit, outstanding, report_quarter, status) VALUES
  (1, 'Sample Steels Ltd',      'Sample Consortium A', 250000000.00, 182500000.00, 'Q3FY2627', 'DUE'),
  (2, 'Sample Textiles Pvt Ltd', 'Sample Consortium A', 75000000.00,  74200000.00, 'Q3FY2627', 'SUBMITTED'),
  (3, 'Sample Logistics LLP',    'Sample Consortium B', 120000000.00,  96100000.00, 'Q3FY2627', 'OVERDUE'),
  (4, 'Sample Chemicals Ltd',    'Sample Consortium B', 45000000.00,  12000000.00, 'Q3FY2627', 'DUE'),
  (5, 'Sample Infra Corp',       'Sample Consortium C', 600000000.00, 588000000.00, 'Q3FY2627', 'SUBMITTED');

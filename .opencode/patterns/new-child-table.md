# New child table of a report

Copy: `eoi_currency_position` in `db/schema-annexii-exposure.sql`, plus the add/delete in
`ReportExposureRepository` and `ReportChildController`.

1. Table in the schema SQL **and** in `db_migrations/01_CREATE_EOI_TABLES.sql` (Oracle). Columns:
   `id`, `report_id` (FK to `eoi_report`), the data, an index on `report_id`.
2. Portable SQL only: no `LIMIT`, no `NVL2`, ids from `eoi_seq.NEXTVAL`.
3. Seed rows only in `h2-init.sql`.
4. Delete SQL carries `AND report_id = ?`. `GuardrailsTest` fails on a `DELETE FROM` without
   `report_id` / `borrower_id`.
5. Follow `new-write-endpoint.md` for the controller, lock config and tests.
6. Update `.brain/03_SYSTEM/Schema.md` and `Change_Map.md`.

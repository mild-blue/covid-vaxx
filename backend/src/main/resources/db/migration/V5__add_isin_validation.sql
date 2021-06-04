-- add isin validation column to patient table
ALTER TABLE patients
    ADD COLUMN is_isin_validated BOOL NOT NULL DEFAULT FALSE;

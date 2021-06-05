-- add isin id column to patient table
ALTER TABLE patients
    ADD COLUMN isin_id VARCHAR(12) DEFAULT NULL;

UPDATE patients
    SET isin_id = 'validated'
    WHERE is_isin_validated = TRUE;

ALTER TABLE patients
    DROP COLUMN is_isin_validated;

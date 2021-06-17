-- add patients.isin_ready column
ALTER TABLE patients
    ADD COLUMN isin_ready BOOL DEFAULT NULL;

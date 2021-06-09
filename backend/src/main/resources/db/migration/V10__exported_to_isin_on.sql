-- add patient_data_correctness.exported_to_isin_on column
ALTER TABLE patient_data_correctness
    ADD COLUMN exported_to_isin_on TIMESTAMPTZ;

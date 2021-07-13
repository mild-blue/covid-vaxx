-- prepare db for second dose of vaccine
ALTER TABLE vaccinations
    ADD COLUMN dose_number INTEGER NOT NULL DEFAULT 1;

ALTER TABLE patients
    ADD COLUMN vaccination_second_dose_id UUID REFERENCES vaccinations (id) ON DELETE SET NULL;

-- Drops UNIQUE constraint of vaccinations.patient_id.
-- The constraint name vaccinations_patient_id_key is not specified in any migration however
-- the standard names in PostgreSQL are {tablename}_{columnname(s)}_{suffix} so this should work.
ALTER TABLE vaccinations
    DROP CONSTRAINT vaccinations_patient_id_key;

ALTER TABLE vaccinations
    ADD UNIQUE (patient_id, dose_number);

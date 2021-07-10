-- add vaccine dose number and patients.vaccination_second_dose_id columns
ALTER TABLE vaccinations
    ADD COLUMN dose_number INTEGER NOT NULL DEFAULT 1;

ALTER TABLE patients
    ADD COLUMN vaccination_second_dose_id UUID REFERENCES vaccinations (id) ON DELETE SET NULL;

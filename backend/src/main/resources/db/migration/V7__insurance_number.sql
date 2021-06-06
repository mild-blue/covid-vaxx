-- add insurance number column
ALTER TABLE patients
    ADD COLUMN insurance_number VARCHAR(256) DEFAULT NULL;

ALTER TABLE patients
    ALTER COLUMN personal_number DROP NOT NULL;

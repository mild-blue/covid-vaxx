-- add insurance number unique constraint
ALTER TABLE patients
    ADD UNIQUE (insurance_number);

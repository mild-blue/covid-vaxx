-- Enable Case-insensitive text

CREATE EXTENSION IF NOT EXISTS citext;

-- BEFORE insert function for trigger

CREATE OR REPLACE FUNCTION set_created() RETURNS TRIGGER
AS
$BODY$
BEGIN
    new.created := NOW();
    new.updated := new.created;
    RETURN new;
END;
$BODY$
    LANGUAGE plpgsql;

-- BEFORE update function for trigger

CREATE OR REPLACE FUNCTION set_updated() RETURNS TRIGGER
AS
$BODY$
BEGIN
    new.updated := NOW();
    RETURN new;
END;
$BODY$
    LANGUAGE plpgsql;

-- Create base schema

-- Create table patients
CREATE TABLE patients
(
    id                VARCHAR(36) UNIQUE NOT NULL,
    created           timestamptz        NOT NULL,
    updated           timestamptz,
    first_name        VARCHAR(256)       NOT NULL,
    last_name         VARCHAR(256)       NOT NULL,
    personal_number   VARCHAR(11)        NOT NULL,
    phone_number      VARCHAR(13)        NOT NULL,
    email             VARCHAR(256)       NOT NULL,
    insurance_company VARCHAR(4)         NOT NULL,
    PRIMARY KEY (id)
);

CREATE TRIGGER tgr_patients_set_created
    BEFORE INSERT
    ON patients
    FOR EACH ROW
EXECUTE PROCEDURE set_created();

CREATE TRIGGER tgr_patients_set_updated
    BEFORE UPDATE
    ON patients
    FOR EACH ROW
EXECUTE PROCEDURE set_updated();

-- Create table questions
CREATE TABLE questions
(
    id          VARCHAR(36) UNIQUE  NOT NULL,
    created     timestamptz         NOT NULL,
    updated     timestamptz,
    placeholder VARCHAR(256) UNIQUE NOT NULL,
    cs          TEXT                NOT NULL,
    eng         TEXT                NOT NULL,
    PRIMARY KEY (id)
);

CREATE TRIGGER tgr_questions_set_created
    BEFORE INSERT
    ON questions
    FOR EACH ROW
EXECUTE PROCEDURE set_created();

CREATE TRIGGER tgr_questions_set_updated
    BEFORE UPDATE
    ON questions
    FOR EACH ROW
EXECUTE PROCEDURE set_updated();

-- Create table answers
CREATE TABLE answers
(
    created     timestamptz NOT NULL,
    updated     timestamptz,
    question_id VARCHAR(36) NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    patient_id  VARCHAR(36) NOT NULL REFERENCES patients (id) ON DELETE CASCADE,
    value       bool        NOT NULL,
    PRIMARY KEY (question_id, patient_id)
);

CREATE TRIGGER tgr_answers_set_created
    BEFORE INSERT
    ON answers
    FOR EACH ROW
EXECUTE PROCEDURE set_created();

CREATE TRIGGER tgr_answers_set_updated
    BEFORE UPDATE
    ON answers
    FOR EACH ROW
EXECUTE PROCEDURE set_updated();

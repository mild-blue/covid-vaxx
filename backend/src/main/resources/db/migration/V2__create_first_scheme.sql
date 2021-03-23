-- Create table users
CREATE TABLE users
(
    id            UUID PRIMARY KEY    NOT NULL DEFAULT uuid_generate_v4(),
    created       TIMESTAMPTZ         NOT NULL,
    updated       TIMESTAMPTZ         NOT NULL,
    email         VARCHAR(256) UNIQUE NOT NULL,
    password_hash VARCHAR(128)        NOT NULL,
    role          VARCHAR(16)         NOT NULL
);

CREATE TRIGGER tgr_users_set_created
    BEFORE INSERT
    ON users
    FOR EACH ROW
EXECUTE PROCEDURE set_created();

CREATE TRIGGER tgr_users_set_updated
    BEFORE UPDATE
    ON users
    FOR EACH ROW
EXECUTE PROCEDURE set_updated();

-- Create table nurses
CREATE TABLE nurses
(
    id         UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    created    TIMESTAMPTZ      NOT NULL,
    updated    TIMESTAMPTZ      NOT NULL,
    first_name VARCHAR(256)     NOT NULL,
    last_name  VARCHAR(256)     NOT NULL,
    email      VARCHAR(256)     NOT NULL
);

CREATE TRIGGER tgr_nurses_set_created
    BEFORE INSERT
    ON nurses
    FOR EACH ROW
EXECUTE PROCEDURE set_created();

CREATE TRIGGER tgr_nurses_set_updated
    BEFORE UPDATE
    ON nurses
    FOR EACH ROW
EXECUTE PROCEDURE set_updated();

-- Create table user logins
CREATE TABLE user_logins
(
    id                    UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    created               TIMESTAMPTZ      NOT NULL,
    updated               TIMESTAMPTZ      NOT NULL,
    user_id               UUID             NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    vaccine_serial_number VARCHAR(256),
    nurse_id              UUID             REFERENCES nurses (id) ON DELETE SET NULL,
    success               BOOLEAN          NOT NULL
);

CREATE TRIGGER tgr_user_logins_set_created
    BEFORE INSERT
    ON user_logins
    FOR EACH ROW
EXECUTE PROCEDURE set_created();

CREATE TRIGGER tgr_user_logins_set_updated
    BEFORE UPDATE
    ON user_logins
    FOR EACH ROW
EXECUTE PROCEDURE set_updated();

-- Create table patients
CREATE TABLE patients
(
    id                  UUID PRIMARY KEY   NOT NULL DEFAULT uuid_generate_v4(),
    created             TIMESTAMPTZ        NOT NULL,
    updated             TIMESTAMPTZ        NOT NULL,
    first_name          VARCHAR(256)       NOT NULL,
    last_name           VARCHAR(256)       NOT NULL,
    zip_code            INTEGER            NOT NULL,
    district            VARCHAR(128)       NOT NULL,
    personal_number     VARCHAR(11) UNIQUE NOT NULL,
    phone_number        VARCHAR(13)        NOT NULL,
    email               VARCHAR(256)       NOT NULL,
    insurance_company   VARCHAR(4)         NOT NULL,
    remote_host         VARCHAR(45)        NOT NULL,
    email_sent_date     TIMESTAMPTZ,
    data_correctness_id UUID               REFERENCES patient_data_correctness (id) ON DELETE SET NULL,
    vaccination_id      UUID               REFERENCES vaccinations (id) ON DELETE SET NULL
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

-- Create data correctness table
CREATE TABLE patient_data_correctness
(
    id                   UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    created              TIMESTAMPTZ      NOT NULL,
    updated              TIMESTAMPTZ      NOT NULL,
    patient_id           UUID             NOT NULL UNIQUE REFERENCES patients (id) ON DELETE CASCADE,
    user_performed_check UUID             NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    data_are_correct     BOOL             NOT NULL,
    notes                TEXT
);

CREATE TRIGGER tgr_patient_data_correctness_set_created
    BEFORE INSERT
    ON patient_data_correctness
    FOR EACH ROW
EXECUTE PROCEDURE set_created();

CREATE TRIGGER tgr_patient_data_correctness_set_updated
    BEFORE UPDATE
    ON patient_data_correctness
    FOR EACH ROW
EXECUTE PROCEDURE set_updated();

-- Create table vaccinations
CREATE TABLE vaccinations
(
    id                    UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    created               TIMESTAMPTZ      NOT NULL,
    updated               TIMESTAMPTZ      NOT NULL,
    patient_id            UUID UNIQUE REFERENCES patients (id) ON DELETE CASCADE,
    body_part             VARCHAR(17)      NOT NULL,
    vaccine_serial_number VARCHAR(256)     NOT NULL,
    user_performing       UUID             NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    nurse_id              UUID             REFERENCES nurses (id) ON DELETE SET NULL,
    notes                 TEXT
);

CREATE TRIGGER tgr_vaccinations_set_created
    BEFORE INSERT
    ON vaccinations
    FOR EACH ROW
EXECUTE PROCEDURE set_created();

CREATE TRIGGER tgr_vaccinations_set_updated
    BEFORE UPDATE
    ON vaccinations
    FOR EACH ROW
EXECUTE PROCEDURE set_updated();

-- Create table questions
CREATE TABLE questions
(
    id          UUID PRIMARY KEY    NOT NULL DEFAULT uuid_generate_v4(),
    created     TIMESTAMPTZ         NOT NULL,
    updated     TIMESTAMPTZ         NOT NULL,
    placeholder VARCHAR(256) UNIQUE NOT NULL,
    cs          TEXT                NOT NULL,
    eng         TEXT                NOT NULL
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
    created     TIMESTAMPTZ NOT NULL,
    updated     TIMESTAMPTZ NOT NULL,
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

-- Create table locations
CREATE TABLE locations
(
    id                UUID PRIMARY KEY   NOT NULL DEFAULT uuid_generate_v4(),
    created           TIMESTAMPTZ        NOT NULL,
    updated           TIMESTAMPTZ        NOT NULL,
    address           VARCHAR(256)       NOT NULL,
    zip_code          INTEGER            NOT NULL,
    district          VARCHAR(128)       NOT NULL,
    phone_number      VARCHAR(13)        NOT NULL,
    email             VARCHAR(256)       NOT NULL,
    notes             TEXT
);

CREATE TRIGGER tgr_locations_set_created
    BEFORE INSERT
    ON locations
    FOR EACH ROW
EXECUTE PROCEDURE set_created();

CREATE TRIGGER tgr_locations_set_updated
    BEFORE UPDATE
    ON locations
    FOR EACH ROW
EXECUTE PROCEDURE set_updated();

-- Create vaccination slots table
CREATE TABLE vaccination_slots
(
    id                   UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    created              TIMESTAMPTZ      NOT NULL,
    updated              TIMESTAMPTZ      NOT NULL,
    location_id          UUID             NOT NULL REFERENCES locations (id) ON DELETE CASCADE,
    patient_id           UUID             REFERENCES patients (id) ON DELETE SET NULL,
    "queue"              INTEGER          NOT NULL,
    "from"               TIMESTAMPTZ      NOT NULL,
    "to"                 TIMESTAMPTZ      NOT NULL,
    UNIQUE(location_id, "queue", "from", "to")
);

CREATE TRIGGER tgr_vaccination_slots_set_created
    BEFORE INSERT
    ON vaccination_slots
    FOR EACH ROW
EXECUTE PROCEDURE set_created();

CREATE TRIGGER tgr_vaccination_slots_set_updated
    BEFORE UPDATE
    ON vaccination_slots
    FOR EACH ROW
EXECUTE PROCEDURE set_updated();

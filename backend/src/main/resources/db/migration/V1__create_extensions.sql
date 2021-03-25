-- Enable Case-insensitive text

CREATE EXTENSION IF NOT EXISTS citext;

-- Enable generating UUIDs.
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


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

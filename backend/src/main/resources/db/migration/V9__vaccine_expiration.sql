-- add vaccine expiration column
ALTER TABLE user_logins
    ADD COLUMN vaccine_expiration DATE DEFAULT NULL;

ALTER TABLE vaccinations
    ADD COLUMN vaccine_expiration DATE DEFAULT NULL;

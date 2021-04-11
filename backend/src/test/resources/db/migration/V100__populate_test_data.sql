-- noinspection SqlInsertValuesForFile

-- data for unit tests

-- email: vaxx@mild.blue
-- password": bluemild
INSERT INTO users (id, first_name, last_name, email, password_hash, "role")
VALUES ('c3858476-3934-4727-82f5-f9d42cea4adb', 'Mild', 'Blue', 'vaxx@mild.blue',
        '$s0$e0801$asDyD5znh458o/+vCMIaLw==$zydsv6Cw2fKxkIGqFNFMDWQ47pKdHIInLURYOeVlYuA=', 'ADMIN');

INSERT INTO nurses (id, first_name, last_name, email)
VALUES ('3449bc47-1e02-4353-9d86-c9b4acf8889e', 'John', 'Doe', 'john.doe@mild.blue'),
       ('5a1c1110-c485-4822-95a3-de58ef6c0dca', 'Amanda', 'Smith', 'amanda.smith@mild.blue'),
       ('f3ca381e-899c-4abf-bbee-3cc2ca7dc23c', 'Alice', 'B', 'alice.b@mild.blue');

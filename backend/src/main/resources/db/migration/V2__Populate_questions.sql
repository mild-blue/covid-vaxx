-- initial question data
insert into questions (id, placeholder, cs, eng)
values ('9a5587a1-dc43-49f3-9847-b736127c9e39', 'is_sick', 'Cítíte se v tuto chvílí nemocný/á', ''),
       ('f74ebe1e-ef94-4af0-963d-97ffab086b6b', 'had_covid', 'Prodělal/a jste onemocnění COVID-19 nebo jste měl/a pozitivní PCR test?', ''),
       ('f68d221d-27a1-4c81-bf45-07b1f0290e15', 'is_vaccinated_against_covid', 'Byl/a jste již očkován/a proti nemoci COVID-19?', ''),
       ('f9c99047-0f44-4dfe-9964-71274a7af5e9', 'had_allergic_reaction', 'Měl/a jste někdy závažnou alergickou reakci po očkování?', ''),
       ('f5cf0689-a4d7-4c42-8107-6eaedca88a93', 'has_blood_problems', 'Máte nějakou krvácivou poruchu nebo berete léky na ředění krve?',
        ''),
       ('7b02b12a-abb4-45d3-8bf4-0b074e445f37', 'has_immunity_problems', 'Máte nějakou závažnou poruchu imunity?', ''),
       ('112f5fbd-cde2-4fe9-8cab-f5b4fff57296', 'is_pregnant', 'Jste těhotná nebo kojíte?', ''),
       ('f4ca8d25-faaa-4b2f-abc2-3d7a8702d4a3', 'is_vaccinated_in_last_two_weeks',
        'Absolvovala jste v posledních dvou týdnech nějaké jiné očkování?', '');

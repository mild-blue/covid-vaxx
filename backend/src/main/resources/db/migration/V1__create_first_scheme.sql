create table patients
(
    id                varchar(36) unique not null,
    first_name        varchar(256)       not null,
    last_name         varchar(256)       not null,
    personal_number   varchar(11)        not null,
    phone_number      varchar(13)        not null,
    email             varchar(256)       not null,
    insurance_company varchar(4)         not null,
    primary key (id)
);

create table questions
(
    id          varchar(36) unique  not null,
    placeholder varchar(256) unique not null,
    cs          text                not null,
    eng         text                not null,
    primary key (id)
);

create table answers
(
    question_id varchar(36) not null references questions (id) on delete cascade,
    patient_id  varchar(36) not null references patients (id) on delete cascade,
    value       bool        not null,
    primary key (question_id, patient_id)
)

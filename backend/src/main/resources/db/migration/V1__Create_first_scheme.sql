create table patients
(
    id              varchar(36) unique not null,
    first_name      varchar(256)       not null,
    last_name       varchar(256)       not null,
    personal_number varchar(11)        not null,
    phone_number    varchar(13)        not null,
    email           varchar(256)       not null,
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

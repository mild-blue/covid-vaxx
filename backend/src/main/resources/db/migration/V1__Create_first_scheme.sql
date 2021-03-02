create table patient
(
    id             varchar(36) unique not null,
    firstName      varchar(256)       not null,
    lastName       varchar(256)       not null,
    personalNumber varchar(11)        not null,
    phoneNumber    varchar(13)        not null,
    email          varchar(256)       not null,
    primary key (id)
);

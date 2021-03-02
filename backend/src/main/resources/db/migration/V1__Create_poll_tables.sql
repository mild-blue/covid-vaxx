create table patient
(
    id   varchar(36) unique not null,
    name varchar(256)       not null,
    primary key (id)
);

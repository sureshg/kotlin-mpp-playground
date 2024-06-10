create table if not exists author
(
    id   bigserial primary key,
    name varchar not null
);

create table if not exists book
(
    id          serial primary key,
    title       varchar      not null,
    description varchar(256) not null    default '',
    created     timestamp with time zone default current_timestamp,
    author      bigint       not null references author (id)
);
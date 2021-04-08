create table document
(
    id            bigserial primary key,
    filepath      text         not null,
    original_name varchar(255) not null,
    name          varchar(255)
);
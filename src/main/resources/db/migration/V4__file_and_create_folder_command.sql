create table file
(
    id        uuid primary key,
    parent_id uuid,
    is_folder boolean      not null,
    name      varchar(255) not null
);

create table remarkable_command
(
    file_id        uuid    not null,
    command_number bigint  not null,
    applied        boolean not null,

    primary key (file_id, command_number)
);

create table create_folder_command
(
    file_id        uuid         not null,
    command_number bigint       not null,
    name           varchar(255) not null,

    foreign key (file_id, command_number) references remarkable_command (file_id, command_number)
);
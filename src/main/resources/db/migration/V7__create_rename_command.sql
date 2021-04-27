create table rename_command
(
    file_id        uuid         not null,
    command_number bigint       not null,
    new_name       varchar(255) not null,

    foreign key (file_id, command_number) references remarkable_command (file_id, command_number)
);
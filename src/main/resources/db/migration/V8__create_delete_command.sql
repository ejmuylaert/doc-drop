create table delete_command
(
    file_id        uuid   not null,
    command_number bigint not null,

    foreign key (file_id, command_number) references remarkable_command (file_id, command_number)
);
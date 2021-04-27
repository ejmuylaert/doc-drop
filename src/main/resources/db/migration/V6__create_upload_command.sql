create table upload_file_command
(
    file_id        uuid         not null,
    command_number bigint       not null,
    name           varchar(255) not null,
    parent_id      uuid,

    foreign key (file_id, command_number) references remarkable_command (file_id, command_number)
);
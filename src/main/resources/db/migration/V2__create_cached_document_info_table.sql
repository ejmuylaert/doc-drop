create table cached_document_info
(
    id        uuid primary key,
    parent_id uuid,
    is_folder boolean not null,
    name      varchar(255)
);
alter table remarkable_command rename to sync_command;

alter table sync_command drop column execution_started_at;
alter table sync_command drop column executed_at;

create type sync_result as enum ('SUCCESS', 'PRE_CONDITION_FAILED', 'EXECUTION_FAILED');

alter table sync_command add column created_at   timestamp    not null;
alter table sync_command add column synced_at    timestamp    default null;
alter table sync_command add column sync_result  sync_result  default null;
alter table sync_command add column sync_message varchar(250) default null;

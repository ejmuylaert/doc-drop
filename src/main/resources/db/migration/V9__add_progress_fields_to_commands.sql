alter table remarkable_command drop column applied;
alter table remarkable_command add column execution_started_at timestamp default null;
alter table remarkable_command add column executed_at          timestamp default null;
# --- !Ups
ALTER TYPE project_status RENAME VALUE 'archived' TO 'deleted';

# --- !Downs
ALTER TYPE project_status RENAME VALUE 'deleted' TO 'archived';
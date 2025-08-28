# --- !Ups
CREATE TYPE column_status AS ENUM ('active', 'archived', 'deleted');

ALTER TABLE "columns"
ADD COLUMN "status" column_status DEFAULT 'active';

ALTER TABLE "columns"
ADD CONSTRAINT "unique_project_name"
UNIQUE ("project_id", "name");

ALTER TABLE "columns"
ADD CONSTRAINT "unique_project_position"
UNIQUE ("project_id", "position");


# --- !Downs
DROP TYPE column_status;

ALTER TABLE "columns"
DROP COLUMN "status";

ALTER TABLE "columns"
DROP CONSTRAINT "unique_project_name";

ALTER TABLE "columns"
DROP CONSTRAINT "unique_project_position";


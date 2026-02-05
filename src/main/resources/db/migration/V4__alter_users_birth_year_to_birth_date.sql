-- V4__alter_users_birth_year_to_birth_date.sql
-- Description: Change birth_year (INT) to birth_date (DATE)

ALTER TABLE users
    DROP COLUMN birth_year;

ALTER TABLE users
    ADD COLUMN birth_date DATE;

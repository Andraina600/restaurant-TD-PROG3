CREATE DATABASE mini_dish_db;
CREATE USER mini_dish_db_manager WITH PASSWORD'123456';
grant connect on database mini_dish_db to mini_dish_db_manager;
GRANT CREATE ON SCHEMA public to mini_dish_db;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO mini_dish_db_manager;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT, UPDATE, ON SEQUENCES TO mini_dish_db_manager;

-- Creates the database used by the LocalisationLab backend.
CREATE DATABASE IF NOT EXISTS localisation
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

-- Selects the database before creating the table.
USE localisation;

-- Recreates the position table with the schema required by the lab.
CREATE TABLE IF NOT EXISTS position (
    id int primary key auto_increment,
    latitude double not null,
    longitude double not null,
    date datetime not null,
    imei varchar(20) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE authorizations (
  id           INT(11)      NOT NULL PRIMARY KEY AUTO_INCREMENT,
  access_group VARCHAR(255) NOT NULL UNIQUE,
  role         VARCHAR(255) NOT NULL UNIQUE
);
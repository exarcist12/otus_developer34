-- Для @GeneratedValue(strategy = GenerationType.IDENTITY)

CREATE TABLE client (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(255),
                        address_id BIGINT UNIQUE
);

CREATE TABLE address (
                         id BIGSERIAL PRIMARY KEY,
                         street VARCHAR(255)
);

CREATE TABLE phone (
                       id BIGSERIAL PRIMARY KEY,
                       number VARCHAR(50),
                       client_id BIGINT
);

-- Внешние ключи
ALTER TABLE client ADD CONSTRAINT fk_client_address
    FOREIGN KEY (address_id) REFERENCES address(id) ON DELETE CASCADE;

ALTER TABLE phone ADD CONSTRAINT fk_phone_client
    FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE CASCADE;



-- Для @GeneratedValue(strategy = GenerationType.SEQUENCE)
-- create sequence client_SEQ start with 1 increment by 1;
--
-- create table client
-- (
--     id   bigint not null primary key,
--     name varchar(50)
-- );
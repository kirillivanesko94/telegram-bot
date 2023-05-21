--liquibase formatted sql

--changeset kivanesko:1
CREATE table notification_task(
    id BIGINT PRIMARY KEY,
    text TEXT,
    date TIMESTAMP
)
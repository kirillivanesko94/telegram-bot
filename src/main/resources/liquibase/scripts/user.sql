--liquibase formatted sql

--changeset kivanesko:1
CREATE table notification_task(
    id BIGINT PRIMARY KEY,
    text TEXT,
    date TIMESTAMP
)
--changeset kivanesko:2
ALTER TABLE notification_task ADD chatId BIGINT;

--changeset kivanesko:3
ALTER TABLE notification_task RENAME chatId TO id_chat;
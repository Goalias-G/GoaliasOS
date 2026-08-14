ALTER TABLE chat_session
    ADD COLUMN archive_status TINYINT NOT NULL DEFAULT 0 COMMENT '归档状态（0：正常，1：已归档）' AFTER conversation_id;

ALTER TABLE v1_delivra_service.chat_messages
    ALTER COLUMN message_text DROP NOT NULL;

ALTER TABLE v1_delivra_service.chat_messages
    ADD COLUMN IF NOT EXISTS file_url  VARCHAR(500),
    ADD COLUMN IF NOT EXISTS file_name VARCHAR(255);

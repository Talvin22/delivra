-- Navigation sessions: tracks active driver navigation for a delivery task
CREATE TABLE navigation_sessions
(
    id                  SERIAL PRIMARY KEY,
    delivery_task_id    INTEGER          NOT NULL,
    current_latitude    DOUBLE PRECISION,
    current_longitude   DOUBLE PRECISION,
    encoded_polyline    TEXT,
    status              VARCHAR(30)      NOT NULL DEFAULT 'ACTIVE',
    started_at          TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at            TIMESTAMP,
    created             TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated             TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nav_session_task FOREIGN KEY (delivery_task_id) REFERENCES delivery_tasks (id) ON DELETE CASCADE
);

CREATE INDEX idx_nav_session_task ON navigation_sessions (delivery_task_id);

-- Chat messages: persistent messages between driver and dispatcher within a task context
CREATE TABLE chat_messages
(
    id               SERIAL PRIMARY KEY,
    delivery_task_id INTEGER     NOT NULL,
    sender_id        INTEGER     NOT NULL,
    message_text     TEXT        NOT NULL,
    is_read          BOOLEAN     NOT NULL DEFAULT false,
    created          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted          BOOLEAN     NOT NULL DEFAULT false,
    CONSTRAINT fk_chat_task FOREIGN KEY (delivery_task_id) REFERENCES delivery_tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_sender FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_task_created ON chat_messages (delivery_task_id, created);

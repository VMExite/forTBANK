CREATE TABLE outbox_event (
    event_id BIGSERIAL PRIMARY KEY NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now(),
    retry_count INT NOT NULL DEFAULT 0,
    retry_time TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    type VARCHAR(20)
)

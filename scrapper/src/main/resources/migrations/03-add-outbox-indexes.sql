CREATE INDEX idx_outbox_event_news
ON outbox_event (status, retry_time)
WHERE status = 'NEW';

CREATE TABLE IF NOT EXISTS outbox_events(
	id UUID PRIMARY KEY,
	aggregate_id TEXT NOT NULL,
	topic TEXT NOT NULL,
	event_type TEXT NOT NULL,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	payload TEXT NOT NULL,
	retry_count INTEGER NOT NULL DEFAULT 0,
	last_error TEXT
);

CREATE INDEX idx_outbox_events_created_at ON outbox_events(created_at ASC);

ALTER TABLE outbox_events RENAME COLUMN aggregate_type TO topic;
UPDATE outbox_events SET topic = 'orders.events' WHERE topic = 'Order';

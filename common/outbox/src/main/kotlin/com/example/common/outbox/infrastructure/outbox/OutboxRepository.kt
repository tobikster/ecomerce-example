package com.example.common.outbox.infrastructure.outbox

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.util.UUID

@Repository
class OutboxRepository(
	private val jdbcClient: JdbcClient,
) {
	fun save(event: OutboxEvent) {
		jdbcClient
			.sql(
				"""
				INSERT INTO outbox_events (id, aggregate_id, topic, event_type, created_at, payload, retry_count, last_error)
				VALUES (:id, :aggregateId, :topic, :eventType, :createdAt, :payload, :retryCount, :lastError);
				""".trimIndent(),
			).param("id", event.id)
			.param("aggregateId", event.aggregateId)
			.param("topic", event.topic)
			.param("eventType", event.eventType)
			.param("createdAt", Timestamp.from(event.createdAt))
			.param("payload", event.payload)
			.param("retryCount", event.retryCount)
			.param("lastError", event.lastError)
			.update()
	}

	fun findUnprocessedEvents(batchSize: Int): List<OutboxEvent> =
		jdbcClient
			.sql(
				"""
				SELECT id, aggregate_id, topic, event_type, created_at, payload, retry_count, last_error
				FROM outbox_events
				ORDER BY created_at ASC
				LIMIT :batchSize
				FOR UPDATE SKIP LOCKED
				""".trimIndent(),
			).param("batchSize", batchSize)
			.query { result, _ ->
				OutboxEvent(
					id = result.getObject("id", UUID::class.java),
					aggregateId = result.getString("aggregate_id"),
					topic = result.getString("topic"),
					eventType = result.getString("event_type"),
					createdAt = result.getTimestamp("created_at").toInstant(),
					payload = result.getString("payload"),
					retryCount = result.getInt("retry_count"),
					lastError = result.getString("last_error"),
				)
			}.list()

	fun updateRetry(
		id: UUID,
		retryCount: Int,
		lastError: String?,
	) {
		jdbcClient
			.sql(
				"""
				UPDATE outbox_events
				SET retry_count = :retry_count, last_error = :lastError
				WHERE id = :id
				""".trimIndent(),
			).param("id", id)
			.param("retry_count", retryCount)
			.param("lastError", lastError)
			.update()
	}

	fun delete(ids: List<UUID>) {
		if (ids.isEmpty()) return

		jdbcClient
			.sql("DELETE FROM outbox_events WHERE id in (:ids)")
			.param("ids", ids)
			.update()
	}
}

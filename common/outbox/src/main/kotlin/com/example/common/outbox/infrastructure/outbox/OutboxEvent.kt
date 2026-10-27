package com.example.common.outbox.infrastructure.outbox

import java.time.Instant
import java.util.UUID

data class OutboxEvent(
	val id: UUID = UUID.randomUUID(),
	val aggregateId: String,
	val topic: String,
	val eventType: String,
	val createdAt: Instant = Instant.now(),
	val payload: String,
	val retryCount: Int = 0,
	val lastError: String? = null,
)

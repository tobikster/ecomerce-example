package com.example.common.outbox.infrastructure.outbox

import jakarta.transaction.Transactional
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.KafkaException
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Component
class KafkaOutboxPoller(
	private val outboxRepository: OutboxRepository,
	private val kafkaTemplate: KafkaTemplate<String, String>,
) {
	@Scheduled(fixedDelay = 1000)
	@Transactional
	fun processEvents() {
		val eventsToProcess = outboxRepository.findUnprocessedEvents(EVENTS_BATCH_SIZE)
		buildList {
			eventsToProcess.forEach { event ->
				val topic = event.topic

				try {
					@Suppress("MagicNumber")
					ProducerRecord(topic, event.aggregateId, event.payload)
						.apply {
							headers()
								.add("eventType", event.eventType.toByteArray())
								.add("eventId", event.id.toString().toByteArray())
						}.let(kafkaTemplate::send)
						.get(5, TimeUnit.SECONDS)
					add(event.id)
				} catch (_: InterruptedException) {
					Thread.currentThread().interrupt()
					return
				} catch (e: KafkaException) {
					handleKafkaSendException(event, e)
				} catch (e: ExecutionException) {
					handleKafkaSendException(event, e)
				} catch (e: TimeoutException) {
					handleKafkaSendException(event, e)
				}
			}
		}.takeIf(List<UUID>::isNotEmpty)
			?.also(outboxRepository::delete)
	}

	private fun handleKafkaSendException(
		event: OutboxEvent,
		e: Exception,
	) {
		outboxRepository.updateRetry(
			event.id,
			retryCount = event.retryCount + 1,
			lastError = e.cause?.message ?: e.message,
		)
	}

	companion object {
		const val EVENTS_BATCH_SIZE = 50
	}
}

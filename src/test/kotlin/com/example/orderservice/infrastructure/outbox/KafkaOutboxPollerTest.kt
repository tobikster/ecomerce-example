package com.example.orderservice.infrastructure.outbox

import com.example.orderservice.order.infrastructure.outbox.OrderOutboxEventPublisher
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeoutException

class KafkaOutboxPollerTest {
	private val outboxRepository: OutboxRepository = mock()
	private val kafkaTemplate: KafkaTemplate<String, String> = mock()
	private val poller = KafkaOutboxPoller(outboxRepository, kafkaTemplate)

	@Test
	fun `should publish event to Kafka and delete it from outbox when successful`() {
		// Given
		val eventId = UUID.randomUUID()
		val now = Instant.now()
		given { outboxRepository.findUnprocessedEvents(any()) }
			.willReturn(listOf(createOutboxEvent(id = eventId, createdAt = now)))
		whenever(kafkaTemplate.send(any<ProducerRecord<String, String>>()))
			.thenReturn(CompletableFuture.completedFuture(mock()))

		// When
		poller.processEvents()

		// Then
		verify(kafkaTemplate).send(
			check<ProducerRecord<String, String>> { record ->
				record.topic() shouldBe "orders.events"
				record.key() shouldBe "event_1"
				record.value() shouldBe "payload"

				val eventTypeHeader =
					record
						.headers()
						.lastHeader("eventType")
						?.value()
						?.let(::String)
				eventTypeHeader shouldBe OrderOutboxEventPublisher.EVENT_TYPE_ORDER_CREATED

				val eventIdHeader =
					record
						.headers()
						.lastHeader("eventId")
						?.value()
						?.let(::String)
				eventIdHeader shouldBe eventId.toString()
			},
		)
		verify(outboxRepository).delete(listOf(eventId))
		verify(outboxRepository, never()).updateRetry(eq(eventId), any(), any())
	}

	@Test
	fun `should update retry count when kafka sending fails with execution exception`() {
		// Given
		val eventId = UUID.randomUUID()
		val event = createOutboxEvent(id = eventId, retryCount = 1)
		given { outboxRepository.findUnprocessedEvents(any()) }.willReturn(listOf(event))
		whenever(kafkaTemplate.send(any<ProducerRecord<String, String>>()))
			.thenReturn(CompletableFuture.failedFuture(RuntimeException("Broker not available")))

		// When
		poller.processEvents()

		// Then
		verify(outboxRepository).updateRetry(eventId, retryCount = 2, lastError = "Broker not available")
		verify(outboxRepository, never()).delete(any())
	}

	@Test
	fun `should update retry count when kafka sending times out`() {
		// Given
		val eventId = UUID.randomUUID()
		val event = createOutboxEvent(id = eventId, retryCount = 0)
		val future: CompletableFuture<SendResult<String, String>> = mock()
		whenever(future.get(any(), any())).thenThrow(TimeoutException("Connection timed out"))
		given { outboxRepository.findUnprocessedEvents(any()) }.willReturn(listOf(event))
		whenever(kafkaTemplate.send(any<ProducerRecord<String, String>>())).thenReturn(future)

		// When
		poller.processEvents()

		// Then
		verify(outboxRepository).updateRetry(eventId, retryCount = 1, lastError = "Connection timed out")
		verify(outboxRepository, never()).delete(any())
	}

	@Test
	fun `should handle interruptedException and interrupt thread`() {
		// Given
		val eventId = UUID.randomUUID()
		val event = createOutboxEvent(id = eventId)
		val future: CompletableFuture<SendResult<String, String>> = mock()
		whenever(future.get(any(), any())).thenThrow(InterruptedException())
		given { outboxRepository.findUnprocessedEvents(any()) }.willReturn(listOf(event))
		whenever(kafkaTemplate.send(any<ProducerRecord<String, String>>())).thenReturn(future)

		try {
			// When
			poller.processEvents()

			// Then
			Thread.currentThread().isInterrupted shouldBe true
			verify(outboxRepository, never()).delete(any())
			verify(outboxRepository, never()).updateRetry(any(), any(), any())
		} finally {
			// Clear interrupted status to avoid polluting other tests
			Thread.interrupted()
		}
	}

	@Test
	fun `should process multiple events and only delete successful ones`() {
		// Given
		val successEventId = UUID.randomUUID()
		val failedEventId = UUID.randomUUID()
		val successEvent = createOutboxEvent(id = successEventId, aggregateId = "order_1")
		val failedEvent = createOutboxEvent(id = failedEventId, aggregateId = "order_2", retryCount = 0)

		given { outboxRepository.findUnprocessedEvents(any()) }
			.willReturn(listOf(successEvent, failedEvent))

		whenever(
			kafkaTemplate.send(
				argThat<ProducerRecord<String, String>> {
					key() == "order_1"
				},
			),
		).thenReturn(CompletableFuture.completedFuture(mock()))

		whenever(
			kafkaTemplate.send(
				argThat<ProducerRecord<String, String>> {
					key() == "order_2"
				},
			),
		).thenReturn(CompletableFuture.failedFuture(RuntimeException("Kafka error")))

		// When
		poller.processEvents()

		// Then
		verify(outboxRepository).delete(listOf(successEventId))
		verify(outboxRepository).updateRetry(failedEventId, retryCount = 1, lastError = "Kafka error")
	}

	@Test
	fun `should successfully publish event and delete it after kafka connection is restored`() {
		// Given - Cycle 1: Kafka is down
		val eventId = UUID.randomUUID()
		val initialEvent = createOutboxEvent(id = eventId, retryCount = 0)

		given { outboxRepository.findUnprocessedEvents(any()) }
			.willReturn(listOf(initialEvent))
		whenever(kafkaTemplate.send(any<ProducerRecord<String, String>>()))
			.thenReturn(CompletableFuture.failedFuture(RuntimeException("Connection refused")))

		// When - Cycle 1 executes
		poller.processEvents()

		// Then - Cycle 1: retry count is incremented and error recorded, event is not deleted
		verify(outboxRepository).updateRetry(eventId, retryCount = 1, lastError = "Connection refused")
		verify(outboxRepository, never()).delete(any())

		// Given - Cycle 2: Kafka connection is restored and repository returns updated event
		val retriedEvent = initialEvent.copy(retryCount = 1, lastError = "Connection refused")
		given { outboxRepository.findUnprocessedEvents(any()) }
			.willReturn(listOf(retriedEvent))
		whenever(kafkaTemplate.send(any<ProducerRecord<String, String>>()))
			.thenReturn(CompletableFuture.completedFuture(mock()))

		// When - Cycle 2 executes
		poller.processEvents()

		// Then - Cycle 2: event is successfully published and deleted from outbox
		verify(outboxRepository).delete(listOf(eventId))
	}

	@Suppress("LongParameterList")
	private fun createOutboxEvent(
		id: UUID = UUID.randomUUID(),
		aggregateId: String = "event_1",
		aggregateType: String = OrderOutboxEventPublisher.AGGREGATE_TYPE,
		eventType: String = OrderOutboxEventPublisher.EVENT_TYPE_ORDER_CREATED,
		createdAt: Instant = Instant.now(),
		payload: String = "payload",
		retryCount: Int = 0,
		lastError: String? = null,
	) = OutboxEvent(
		id = id,
		aggregateId = aggregateId,
		aggregateType = aggregateType,
		eventType = eventType,
		createdAt = createdAt,
		payload = payload,
		retryCount = retryCount,
		lastError = lastError,
	)
}

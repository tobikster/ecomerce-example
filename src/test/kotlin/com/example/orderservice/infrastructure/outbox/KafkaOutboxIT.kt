package com.example.orderservice.infrastructure.outbox

import com.example.orderservice.order.domain.model.CustomerId
import com.example.orderservice.order.domain.model.OrderCreatedEvent
import com.example.orderservice.order.domain.model.OrderId
import com.example.orderservice.order.infrastructure.kafka.dto.OrderCreatedEventDto
import com.example.orderservice.order.infrastructure.outbox.OrderOutboxEventPublisher
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.postgresql.PostgreSQLContainer
import tools.jackson.databind.json.JsonMapper
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.util.Currency
import java.util.UUID

@SpringBootTest
@Testcontainers
class KafkaOutboxIT(
	@Autowired private val orderOutboxEventPublisher: OrderOutboxEventPublisher,
	@Autowired private val outboxRepository: OutboxRepository,
	@Autowired private val transactionTemplate: TransactionTemplate,
	@Autowired private val jsonMapper: JsonMapper,
) {
	private lateinit var consumer: KafkaConsumer<String, String>

	@BeforeEach
	fun setUp() {
		val consumerProps =
			mapOf(
				ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers,
				ConsumerConfig.GROUP_ID_CONFIG to "test-group-${UUID.randomUUID()}",
				ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
				ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
				ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
			)
		consumer = KafkaConsumer(consumerProps)
		consumer.subscribe(listOf(ORDERS_TOPIC))

		// Clean outbox table before each test
		val existingEvents = outboxRepository.findUnprocessedEvents(100)
		outboxRepository.delete(existingEvents.map { it.id })
	}

	@AfterEach
	fun tearDown() {
		consumer.close()
	}

	@Test
	fun `should publish event to Kafka via outbox and remove from database when successful`() {
		// Given
		val orderId = OrderId(UUID.randomUUID())
		val event =
			OrderCreatedEvent(
				orderId = orderId,
				customerId = CustomerId("customer_123"),
				amount = BigDecimal("199.99"),
				currency = Currency.getInstance("PLN"),
				createdAt = Instant.parse("2026-10-22T10:00:00Z"),
			)

		// When
		orderOutboxEventPublisher.publish(event)

		// Then - verify event received from Kafka topic
		val record =
			KafkaTestUtils.getSingleRecord(
				consumer,
				ORDERS_TOPIC,
				Duration.ofSeconds(10),
			)

		record.key() shouldBe orderId.value.toString()

		val payload = jsonMapper.readValue(record.value(), OrderCreatedEventDto::class.java)
		payload.orderId shouldBe orderId.value
		payload.customerId shouldBe "customer_123"
		payload.amount shouldBe BigDecimal("199.99")
		payload.currency shouldBe "PLN"
		payload.createdAt shouldBe Instant.parse("2026-10-22T10:00:00Z")

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
		eventIdHeader.shouldNotBeNull()

		// And then - verify outbox table in PostgreSQL is cleaned up by the poller
		await().atMost(Duration.ofSeconds(5)).untilAsserted {
			outboxRepository.findUnprocessedEvents(10).shouldBeEmpty()
		}
	}

	@Test
	fun `should not publish event to Kafka or persist in database when transaction is rolled back`() {
		// Given
		val orderId = OrderId(UUID.randomUUID())
		val event =
			OrderCreatedEvent(
				orderId = orderId,
				customerId = CustomerId("customer_999"),
				amount = BigDecimal("50.00"),
				currency = Currency.getInstance("PLN"),
				createdAt = Instant.now(),
			)

		// When - simulate transaction rollback (e.g. database error or domain exception after publish)
		shouldThrow<IllegalStateException> {
			transactionTemplate.execute {
				orderOutboxEventPublisher.publish(event)
				error("Simulated error triggering database rollback")
			}
		}

		// Then - verify nothing was persisted in PostgreSQL outbox_events
		outboxRepository.findUnprocessedEvents(10).shouldBeEmpty()

		// And verify nothing was sent to Kafka
		val records = consumer.poll(Duration.ofSeconds(2))
		records.isEmpty shouldBe true
	}

	@Test
	fun `should update retry count and last error in PostgreSQL outbox table`() {
		// Given
		val eventId = UUID.randomUUID()
		val event =
			OutboxEvent(
				id = eventId,
				aggregateId = "order_999",
				aggregateType = OrderOutboxEventPublisher.AGGREGATE_TYPE,
				eventType = OrderOutboxEventPublisher.EVENT_TYPE_ORDER_CREATED,
				payload = """{"orderId":"$eventId"}""",
				retryCount = 0,
			)
		outboxRepository.save(event)

		// When
		outboxRepository.updateRetry(
			id = eventId,
			retryCount = 1,
			lastError = "Connection to Kafka broker timed out",
		)

		// Then
		val events = outboxRepository.findUnprocessedEvents(10)
		events shouldHaveSize 1
		events.first().let { updated ->
			updated.id shouldBe eventId
			updated.retryCount shouldBe 1
			updated.lastError shouldBe "Connection to Kafka broker timed out"
		}
	}

	companion object {
		private const val ORDERS_TOPIC = "orders.events"

		@Container
		@ServiceConnection
		val postgres = PostgreSQLContainer("postgres:16-alpine")

		@Container
		@ServiceConnection
		val kafka = KafkaContainer("apache/kafka:4.3.2")
	}
}

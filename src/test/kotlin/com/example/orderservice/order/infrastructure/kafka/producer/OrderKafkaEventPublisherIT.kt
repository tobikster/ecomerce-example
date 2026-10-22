package com.example.orderservice.order.infrastructure.kafka.producer

import com.example.orderservice.order.domain.model.CustomerId
import com.example.orderservice.order.domain.model.OrderCreatedEvent
import com.example.orderservice.order.domain.model.OrderId
import com.example.orderservice.order.infrastructure.kafka.dto.OrderCreatedEventDto
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.kafka.test.utils.KafkaTestUtils
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
class OrderKafkaEventPublisherIT(
	@Autowired private val publisher: OrderKafkaEventPublisher,
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
		consumer.subscribe(listOf(OrderKafkaEventPublisher.ORDERS_EVENTS_TOPIC))
	}

	@AfterEach
	fun tearDown() {
		consumer.close()
	}

	@Test
	fun `should publish OrderCreatedEvent to Kafka topic with correct key and payload`() {
		// Given
		val orderId = OrderId(UUID.randomUUID())
		val event =
			OrderCreatedEvent(
				orderId = orderId,
				customerId = CustomerId("customer_123"),
				amount = BigDecimal("149.99"),
				currency = Currency.getInstance("PLN"),
				createdAt = Instant.parse("2026-10-22T10:00:00Z"),
			)

		// When
		publisher.publish(event)

		// Then
		val record =
			KafkaTestUtils.getSingleRecord(
				consumer,
				OrderKafkaEventPublisher.ORDERS_EVENTS_TOPIC,
				Duration.ofSeconds(10),
			)

		record.key() shouldBe orderId.value.toString()

		val payload = jsonMapper.readValue(record.value(), OrderCreatedEventDto::class.java)
		payload.orderId shouldBe orderId.value
		payload.customerId shouldBe "customer_123"
		payload.amount shouldBe BigDecimal("149.99")
		payload.currency shouldBe "PLN"
		payload.createdAt shouldBe Instant.parse("2026-10-22T10:00:00Z")

		record.headers().lastHeader("__TypeId__").shouldBeNull()
	}

	companion object {
		@Container
		@ServiceConnection
		val postgres = PostgreSQLContainer("postgres:16-alpine")

		@Container
		@ServiceConnection
		val kafka = KafkaContainer("apache/kafka:4.3.2")
	}
}

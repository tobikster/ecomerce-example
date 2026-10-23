package com.example.paymentservice.payment.infrastructure.kafka

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.port.outgoing.AccountRepositoryPort
import com.example.paymentservice.common.domain.model.CustomerId
import com.example.paymentservice.common.domain.model.OrderId
import com.example.paymentservice.common.domain.model.of
import com.example.paymentservice.payment.domain.model.PaymentStatus
import com.example.paymentservice.payment.domain.port.outgoing.PaymentRepositoryPort
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.postgresql.PostgreSQLContainer
import java.math.BigDecimal
import java.time.Duration
import java.util.Currency
import java.util.UUID

@SpringBootTest
@Testcontainers
class OrderEventsKafkaConsumerIT(
	@Autowired private val accountRepository: AccountRepositoryPort,
	@Autowired private val paymentRepository: PaymentRepositoryPort,
) {
	private val pln = Currency.getInstance("PLN")
	private lateinit var producer: KafkaProducer<String, String>

	@BeforeEach
	fun setUp() {
		val props =
			mapOf(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers,
				ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
				ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
			)
		producer = KafkaProducer(props)
	}

	@AfterEach
	fun tearDown() {
		producer.close()
	}

	@Test
	fun `should consume OrderCreated event and successfully complete payment when funds are sufficient`() {
		// Given
		val customerId = CustomerId("cust_e2e_success")
		val initialAccount =
			Account
				.create(customerId, pln)
				.deposit(BigDecimal("200.00") of pln)
		accountRepository.save(initialAccount)

		val orderId = UUID.randomUUID()
		val payload =
			"""
			{
				"orderId": "$orderId",
				"customerId": "${customerId.value}",
				"amount": 75.00,
				"currency": "PLN",
				"createdAt": "2026-10-27T08:00:00Z"
			}
			""".trimIndent()

		// When
		val record = ProducerRecord(OrderEventsKafkaConsumer.ORDERS_TOPIC, orderId.toString(), payload)
		record.headers().add("eventType", OrderEventsKafkaConsumer.EVENT_TYPE_ORDER_CREATED.toByteArray(Charsets.UTF_8))
		record.headers().add("eventId", UUID.randomUUID().toString().toByteArray(Charsets.UTF_8))
		producer.send(record).get()

		// Then
		await().atMost(Duration.ofSeconds(10)).untilAsserted {
			val payment = paymentRepository.findByOrderId(OrderId(orderId))
			payment.shouldNotBeNull()
			payment.status shouldBe PaymentStatus.COMPLETED
			payment.amount.amount.shouldBeEqualComparingTo(BigDecimal("75.00"))
			payment.amount.currency shouldBe pln
			payment.failureReason.shouldBeNull()

			val updatedAccount = accountRepository.findByCustomerIdAndCurrency(customerId, pln)
			updatedAccount.shouldNotBeNull()
			updatedAccount.balance.amount.shouldBeEqualComparingTo(BigDecimal("125.00"))
		}
	}

	@Test
	fun `should consume OrderCreated event and save failed payment when balance is insufficient`() {
		// Given
		val customerId = CustomerId("cust_e2e_insufficient")
		val initialAccount =
			Account
				.create(customerId, pln)
				.deposit(BigDecimal("30.00") of pln)
		accountRepository.save(initialAccount)

		val orderId = UUID.randomUUID()
		val payload =
			"""
			{
				"orderId": "$orderId",
				"customerId": "${customerId.value}",
				"amount": 100.00,
				"currency": "PLN",
				"createdAt": "2026-10-27T08:00:00Z"
			}
			""".trimIndent()

		// When
		val record = ProducerRecord(OrderEventsKafkaConsumer.ORDERS_TOPIC, orderId.toString(), payload)
		record.headers().add("eventType", OrderEventsKafkaConsumer.EVENT_TYPE_ORDER_CREATED.toByteArray(Charsets.UTF_8))
		record.headers().add("eventId", UUID.randomUUID().toString().toByteArray(Charsets.UTF_8))
		producer.send(record).get()

		// Then
		await().atMost(Duration.ofSeconds(10)).untilAsserted {
			val payment = paymentRepository.findByOrderId(OrderId(orderId))
			payment.shouldNotBeNull()
			payment.status shouldBe PaymentStatus.FAILED
			payment.failureReason.shouldNotBeNull()
			payment.failureReason.shouldStartWith("Insufficient funds")

			val updatedAccount = accountRepository.findByCustomerIdAndCurrency(customerId, pln)
			updatedAccount.shouldNotBeNull()
			updatedAccount.balance.amount.shouldBeEqualComparingTo(BigDecimal("30.00"))
		}
	}

	@Test
	fun `should ignore unknown event type and not create payment`() {
		// Given
		val ignoredOrderId = UUID.randomUUID()
		val ignoredPayload = """{"orderId": "$ignoredOrderId"}"""

		val validOrderId = UUID.randomUUID()
		val customerId = CustomerId("cust_e2e_marker")
		accountRepository.save(Account.create(customerId, pln).deposit(BigDecimal("50.00") of pln))
		val validPayload =
			"""
			{
				"orderId": "$validOrderId",
				"customerId": "${customerId.value}",
				"amount": 10.00,
				"currency": "PLN",
				"createdAt": "2026-10-27T08:00:00Z"
			}
			""".trimIndent()

		// When - Send ignored event first
		val ignoredRecord = ProducerRecord(OrderEventsKafkaConsumer.ORDERS_TOPIC, ignoredOrderId.toString(), ignoredPayload)
		ignoredRecord.headers().add("eventType", "OrderCancelled".toByteArray(Charsets.UTF_8))
		producer.send(ignoredRecord).get()

		// Send valid event second as a fence to ensure the topic consumer has reached this point
		val validRecord = ProducerRecord(OrderEventsKafkaConsumer.ORDERS_TOPIC, validOrderId.toString(), validPayload)
		validRecord.headers().add("eventType", OrderEventsKafkaConsumer.EVENT_TYPE_ORDER_CREATED.toByteArray(Charsets.UTF_8))
		producer.send(validRecord).get()

		// Then - once valid order is processed, ignored event must have been skipped
		await().atMost(Duration.ofSeconds(10)).untilAsserted {
			paymentRepository.findByOrderId(OrderId(validOrderId)).shouldNotBeNull()
		}
		paymentRepository.findByOrderId(OrderId(ignoredOrderId)).shouldBeNull()
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

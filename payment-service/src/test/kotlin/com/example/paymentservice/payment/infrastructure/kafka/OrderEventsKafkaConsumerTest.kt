package com.example.paymentservice.payment.infrastructure.kafka

import com.example.paymentservice.common.domain.model.CustomerId
import com.example.paymentservice.common.domain.model.OrderId
import com.example.paymentservice.common.domain.model.of
import com.example.paymentservice.payment.domain.port.incoming.ProcessOrderPaymentUseCase
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.then
import org.mockito.kotlin.verify
import tools.jackson.databind.json.JsonMapper
import java.math.BigDecimal
import java.util.UUID

class OrderEventsKafkaConsumerTest {
	private val processOrderPayment: ProcessOrderPaymentUseCase = mock()
	private val jsonMapper: JsonMapper = JsonMapper.builder().build()
	private val consumer = OrderEventsKafkaConsumer(jsonMapper, processOrderPayment)

	@Test
	fun `should parse OrderCreated event and invoke processOrderPaymentUseCase`() {
		// Given
		val orderId = UUID.randomUUID()
		val payload =
			"""
			{
				"orderId": "$orderId",
				"customerId": "cust_123",
				"amount": 99.99,
				"currency": "PLN",
				"createdAt": "2026-10-27T08:00:00Z"
			}
			""".trimIndent()

		// When
		consumer.handleOrderEvent(
			payload = payload,
			eventType = "OrderCreated",
			eventId = "evt_123",
			key = orderId.toString(),
		)

		// Then
		verify(processOrderPayment).invoke(
			check { command ->
				command.orderId shouldBe OrderId(orderId)
				command.customerId shouldBe CustomerId("cust_123")
				command.amount shouldBe (BigDecimal("99.99") of "PLN")
			},
		)
	}

	@Test
	fun `should ignore event with unknown eventType`() {
		// Given
		val payload = """{"orderId": "${UUID.randomUUID()}"}"""

		// When
		consumer.handleOrderEvent(
			payload = payload,
			eventType = "OrderCancelled",
			eventId = "evt_456",
			key = null,
		)

		// Then
		then(processOrderPayment).shouldHaveNoInteractions()
	}

	@Test
	fun `should ignore event when eventType is null`() {
		// Given
		val payload = """{"orderId": "${UUID.randomUUID()}"}"""

		// When
		consumer.handleOrderEvent(
			payload = payload,
			eventType = null,
			eventId = "evt_789",
			key = null,
		)

		// Then
		then(processOrderPayment).shouldHaveNoInteractions()
	}
}

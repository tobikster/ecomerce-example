package com.example.orderservice.order.infrastructure.kafka

import arrow.core.Either
import com.example.orderservice.order.domain.model.CustomerId
import com.example.orderservice.order.domain.model.Order
import com.example.orderservice.order.domain.model.of
import com.example.orderservice.order.domain.port.incoming.ProcessPaymentFailure
import com.example.orderservice.order.domain.port.incoming.ProcessPaymentUseCase
import com.example.orderservice.order.infrastructure.kafka.dto.PaymentCompletedEventDto
import com.example.orderservice.order.infrastructure.kafka.dto.PaymentFailedEventDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.willReturn
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID

class PaymentEventsKafkaConsumerTest {
	private val jsonMapper = JsonMapper.builder().addModule(kotlinModule()).build()
	private val processPayment: ProcessPaymentUseCase = mock()
	private val consumer = PaymentEventsKafkaConsumer(jsonMapper, processPayment)

	@Test
	fun `should handle PaymentCompleted event successfully`() {
		// Given
		val orderId = UUID.randomUUID()
		val paymentId = UUID.randomUUID()
		val dto =
			PaymentCompletedEventDto(
				paymentId = paymentId,
				orderId = orderId,
				amount = BigDecimal("99.99"),
				currency = "PLN",
			)
		val payload = jsonMapper.writeValueAsString(dto)
		val order = Order.create(CustomerId("c1"), "99.99".toBigDecimal() of Currency.getInstance("PLN"))

		given { processPayment(any()) } willReturn { Either.Right(order) }

		// When
		consumer.handlePaymentEvent(
			payload = payload,
			eventType = PaymentEventsKafkaConsumer.EVENT_TYPE_PAYMENT_COMPLETED,
			eventId = UUID.randomUUID().toString(),
			key = orderId.toString(),
		)

		// Then
		verify(processPayment).invoke(
			check { command ->
				command.orderId.value shouldBe orderId
				command.paymentId.value shouldBe paymentId
				command.isSuccessful shouldBe true
			},
		)
	}

	@Test
	fun `should handle PaymentFailed event successfully`() {
		// Given
		val orderId = UUID.randomUUID()
		val paymentId = UUID.randomUUID()
		val dto =
			PaymentFailedEventDto(
				paymentId = paymentId,
				orderId = orderId,
				amount = BigDecimal("99.99"),
				currency = "PLN",
				failureReason = "Insufficient funds",
			)
		val payload = jsonMapper.writeValueAsString(dto)
		val order = Order.create(CustomerId("c1"), "99.99".toBigDecimal() of Currency.getInstance("PLN"))

		given { processPayment(any()) } willReturn { Either.Right(order) }

		// When
		consumer.handlePaymentEvent(
			payload = payload,
			eventType = PaymentEventsKafkaConsumer.EVENT_TYPE_PAYMENT_FAILED,
			eventId = UUID.randomUUID().toString(),
			key = orderId.toString(),
		)

		// Then
		verify(processPayment).invoke(
			check { command ->
				command.orderId.value shouldBe orderId
				command.paymentId.value shouldBe paymentId
				command.isSuccessful shouldBe false
			},
		)
	}

	@Test
	fun `should throw IllegalStateException when OrderNotFound occurs`() {
		// Given
		val orderId = UUID.randomUUID()
		val paymentId = UUID.randomUUID()
		val dto =
			PaymentCompletedEventDto(
				paymentId = paymentId,
				orderId = orderId,
				amount = BigDecimal("99.99"),
				currency = "PLN",
			)
		val payload = jsonMapper.writeValueAsString(dto)

		given { processPayment(any()) }.willAnswer { invocation ->
			val cmd =
				invocation.arguments[0] as com.example.orderservice.order.domain.port.incoming.ProcessPaymentCommand
			Either.Left(ProcessPaymentFailure.OrderNotFound(cmd.orderId))
		}

		// When & Then
		shouldThrow<IllegalStateException> {
			consumer.handlePaymentEvent(
				payload = payload,
				eventType = PaymentEventsKafkaConsumer.EVENT_TYPE_PAYMENT_COMPLETED,
				eventId = UUID.randomUUID().toString(),
				key = orderId.toString(),
			)
		}
	}

	@Test
	fun `should ignore unknown event type`() {
		// When
		consumer.handlePaymentEvent(
			payload = "{}",
			eventType = "UnknownEvent",
			eventId = UUID.randomUUID().toString(),
			key = "key",
		)

		// Then
		verify(processPayment, never()).invoke(any())
	}
}

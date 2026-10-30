package com.example.orderservice.order.domain.model

import arrow.core.Either
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID

class OrderTest {
	@Test
	fun `should create order when data is valid`() {
		// Given
		val customerId = CustomerId("cust_123")
		val amount = "100.00".toBigDecimal() of Currency.getInstance("PLN")

		// When
		val order = Order.create(customerId, amount)

		// Then
		order.customerId shouldBe customerId
		order.totalAmount shouldBe amount
		order.status shouldBe OrderStatus.CREATED
		order.paymentId shouldBe null
	}

	@Test
	fun `should throw exception when amount is negative or zero`() {
		// Given
		val customerId = CustomerId("cust_123")
		val invalidAmount = BigDecimal("-50.00") of Currency.getInstance("PLN")

		// When & Then
		val exception =
			shouldThrow<OrderException.InvalidAmount> {
				Order.create(customerId, invalidAmount)
			}

		exception.message shouldContain "must be positive"
	}

	@Test
	fun `should confirm payment when order is in created status`() {
		// Given
		val order = Order.create(CustomerId("cust_123"), "100.00".toBigDecimal() of Currency.getInstance("PLN"))
		val paymentId = PaymentId(UUID.randomUUID())

		// When
		val result = order.withConfirmedPayment(paymentId)

		// Then
		val updatedOrder = (result as Either.Right).value
		updatedOrder.status shouldBe OrderStatus.PAID
		updatedOrder.paymentId shouldBe paymentId
	}

	@Test
	fun `should be idempotent when confirming payment with the same paymentId`() {
		// Given
		val paymentId = PaymentId(UUID.randomUUID())
		val order =
			(
				Order
					.create(CustomerId("cust_123"), "100.00".toBigDecimal() of Currency.getInstance("PLN"))
					.withConfirmedPayment(paymentId) as Either.Right
			).value

		// When
		val result = order.withConfirmedPayment(paymentId)

		// Then
		result shouldBe Either.Right(order)
	}

	@Test
	fun `should fail confirming payment when order is already paid with different paymentId`() {
		// Given
		val originalPaymentId = PaymentId(UUID.randomUUID())
		val newPaymentId = PaymentId(UUID.randomUUID())
		val order =
			(
				Order
					.create(CustomerId("cust_123"), "100.00".toBigDecimal() of Currency.getInstance("PLN"))
					.withConfirmedPayment(originalPaymentId) as Either.Right
			).value

		// When
		val result = order.withConfirmedPayment(newPaymentId)

		// Then
		result shouldBe Either.Left(OrderFailure.InvalidStatus(OrderStatus.PAID))
	}

	@Test
	fun `should fail payment and cancel order when in created status`() {
		// Given
		val order = Order.create(CustomerId("cust_123"), "100.00".toBigDecimal() of Currency.getInstance("PLN"))
		val paymentId = PaymentId(UUID.randomUUID())

		// When
		val result = order.withFailedPayment(paymentId)

		// Then
		val updatedOrder = (result as Either.Right).value
		updatedOrder.status shouldBe OrderStatus.CANCELLED
		updatedOrder.paymentId shouldBe paymentId
	}

	@Test
	fun `should be idempotent when failing payment with the same paymentId`() {
		// Given
		val paymentId = PaymentId(UUID.randomUUID())
		val order =
			(
				Order
					.create(CustomerId("cust_123"), "100.00".toBigDecimal() of Currency.getInstance("PLN"))
					.withFailedPayment(paymentId) as Either.Right
			).value

		// When
		val result = order.withFailedPayment(paymentId)

		// Then
		result shouldBe Either.Right(order)
	}
}

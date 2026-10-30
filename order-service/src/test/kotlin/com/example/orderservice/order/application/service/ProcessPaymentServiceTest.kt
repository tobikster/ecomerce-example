package com.example.orderservice.order.application.service

import arrow.core.Either
import com.example.orderservice.order.domain.model.CustomerId
import com.example.orderservice.order.domain.model.Order
import com.example.orderservice.order.domain.model.OrderId
import com.example.orderservice.order.domain.model.OrderStatus
import com.example.orderservice.order.domain.model.PaymentId
import com.example.orderservice.order.domain.model.of
import com.example.orderservice.order.domain.port.incoming.ProcessPaymentCommand
import com.example.orderservice.order.domain.port.incoming.ProcessPaymentFailure
import com.example.orderservice.order.domain.port.outgoing.OrderRepositoryPort
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Currency
import java.util.UUID

class ProcessPaymentServiceTest {
	private val orderRepository: OrderRepositoryPort = mock()
	private val processPaymentService = ProcessPaymentService(orderRepository)

	@Test
	fun `should successfully process payment confirmation and save order`() {
		// Given
		val order = Order.create(CustomerId("cust_1"), "99.99".toBigDecimal() of Currency.getInstance("PLN"))
		val paymentId = PaymentId(UUID.randomUUID())
		val command = ProcessPaymentCommand(order.id, paymentId, isSuccessful = true)

		whenever(orderRepository.findById(order.id)).thenReturn(order)
		whenever(orderRepository.save(any())).thenAnswer { it.arguments[0] as Order }

		// When
		val result = processPaymentService(command)

		// Then
		val updatedOrder = (result as Either.Right).value
		updatedOrder.status shouldBe OrderStatus.PAID
		updatedOrder.paymentId shouldBe paymentId
		verify(orderRepository).save(updatedOrder)
	}

	@Test
	fun `should successfully process payment failure and cancel order`() {
		// Given
		val order = Order.create(CustomerId("cust_1"), "99.99".toBigDecimal() of Currency.getInstance("PLN"))
		val paymentId = PaymentId(UUID.randomUUID())
		val command = ProcessPaymentCommand(order.id, paymentId, isSuccessful = false)

		whenever(orderRepository.findById(order.id)).thenReturn(order)
		whenever(orderRepository.save(any())).thenAnswer { it.arguments[0] as Order }

		// When
		val result = processPaymentService(command)

		// Then
		val updatedOrder = (result as Either.Right).value
		updatedOrder.status shouldBe OrderStatus.CANCELLED
		updatedOrder.paymentId shouldBe paymentId
		verify(orderRepository).save(updatedOrder)
	}

	@Test
	fun `should return OrderNotFound when order does not exist`() {
		// Given
		val orderId = OrderId(UUID.randomUUID())
		val command = ProcessPaymentCommand(orderId, PaymentId(UUID.randomUUID()), isSuccessful = true)

		whenever(orderRepository.findById(orderId)).thenReturn(null)

		// When
		val result = processPaymentService(command)

		// Then
		result shouldBe Either.Left(ProcessPaymentFailure.OrderNotFound(orderId))
		verify(orderRepository, never()).save(any())
	}

	@Test
	fun `should return InvalidOrderStatus when order cannot transition`() {
		// Given
		val initialPaymentId = PaymentId(UUID.randomUUID())
		val order =
			(
				Order
					.create(CustomerId("cust_1"), "99.99".toBigDecimal() of Currency.getInstance("PLN"))
					.withConfirmedPayment(initialPaymentId) as Either.Right
			).value
		val differentPaymentId = PaymentId(UUID.randomUUID())
		val command = ProcessPaymentCommand(order.id, differentPaymentId, isSuccessful = true)

		whenever(orderRepository.findById(order.id)).thenReturn(order)

		// When
		val result = processPaymentService(command)

		// Then
		result shouldBe Either.Left(ProcessPaymentFailure.InvalidOrderStatus(OrderStatus.PAID))
		verify(orderRepository, never()).save(any())
	}
}

package com.example.orderservice.order.application.service

import com.example.orderservice.order.domain.model.Order
import com.example.orderservice.order.domain.port.incoming.CreateOrderCommand
import com.example.orderservice.order.domain.port.outgoing.OrderEventPublisherPort
import com.example.orderservice.order.domain.port.outgoing.OrderRepositoryPort
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.Currency

class CreateOrderServiceTest {
	private val orderRepository: OrderRepositoryPort = mock()
	private val orderEventPublisher: OrderEventPublisherPort = mock()
	private val createOrderService = CreateOrderService(orderRepository, orderEventPublisher)

	@Test
	fun `should save order and publish OrderCreatedEvent`() {
		// Given
		val command =
			CreateOrderCommand(
				customerId = "customer_123",
				amount = BigDecimal("99.99"),
				currency = Currency.getInstance("PLN"),
			)
		whenever(orderRepository.save(any())).thenAnswer { it.arguments[0] as Order }

		// When
		val result = createOrderService(command)

		// Then
		verify(orderRepository).save(any())
		verify(orderEventPublisher).publish(
			check { event ->
				event.orderId shouldBe result.id
				event.customerId.value shouldBe "customer_123"
				event.amount shouldBe BigDecimal("99.99")
				event.currency.currencyCode shouldBe "PLN"
			},
		)
	}
}

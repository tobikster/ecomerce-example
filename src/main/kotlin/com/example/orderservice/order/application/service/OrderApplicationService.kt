package com.example.orderservice.order.application.service

import com.example.orderservice.order.domain.model.CustomerId
import com.example.orderservice.order.domain.model.Order
import com.example.orderservice.order.domain.model.of
import com.example.orderservice.order.domain.port.incoming.CreateOrderCommand
import com.example.orderservice.order.domain.port.incoming.CreateOrderUseCase
import com.example.orderservice.order.domain.port.outgoing.OrderRepositoryPort
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class OrderApplicationService(
	private val orderRepository: OrderRepositoryPort,
) : CreateOrderUseCase {
	@Transactional
	override fun invoke(command: CreateOrderCommand) =
		Order
			.create(
				customerId = CustomerId(command.customerId),
				totalAmount = command.amount of command.currency,
			).let(orderRepository::save)
}

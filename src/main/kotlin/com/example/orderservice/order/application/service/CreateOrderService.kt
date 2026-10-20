package com.example.orderservice.order.application.service

import com.example.orderservice.order.domain.model.CustomerId
import com.example.orderservice.order.domain.model.Order
import com.example.orderservice.order.domain.model.of
import com.example.orderservice.order.domain.port.incoming.CreateOrderCommand
import com.example.orderservice.order.domain.port.incoming.CreateOrderUseCase
import com.example.orderservice.order.domain.port.outgoing.OrderRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateOrderService(
	private val orderRepository: OrderRepositoryPort,
) : CreateOrderUseCase {
	@Transactional
	override operator fun invoke(command: CreateOrderCommand) =
		Order
			.create(
				customerId = CustomerId(command.customerId),
				totalAmount = command.amount of command.currency,
			).let(orderRepository::save)
}

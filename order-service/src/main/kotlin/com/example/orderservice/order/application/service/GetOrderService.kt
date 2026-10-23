package com.example.orderservice.order.application.service

import com.example.orderservice.order.domain.port.incoming.GetOrderQuery
import com.example.orderservice.order.domain.port.incoming.GetOrderUseCase
import com.example.orderservice.order.domain.port.outgoing.OrderRepositoryPort
import org.springframework.stereotype.Service

@Service
class GetOrderService(
	private val orderRepository: OrderRepositoryPort,
) : GetOrderUseCase {
	override fun invoke(query: GetOrderQuery) = orderRepository.findById(query.id)
}

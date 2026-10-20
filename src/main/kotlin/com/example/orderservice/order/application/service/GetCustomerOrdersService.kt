package com.example.orderservice.order.application.service

import com.example.orderservice.order.domain.port.incoming.GetCustomerOrdersQuery
import com.example.orderservice.order.domain.port.incoming.GetCustomerOrdersUseCase
import com.example.orderservice.order.domain.port.outgoing.OrderRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetCustomerOrdersService(
	private val orderRepository: OrderRepositoryPort,
) : GetCustomerOrdersUseCase {
	@Transactional(readOnly = true)
	override fun invoke(query: GetCustomerOrdersQuery) = orderRepository.findByCustomerId(query.customerId)
}

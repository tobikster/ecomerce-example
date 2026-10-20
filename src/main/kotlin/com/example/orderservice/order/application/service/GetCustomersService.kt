package com.example.orderservice.order.application.service

import com.example.orderservice.order.domain.port.incoming.GetCustomersUseCase
import com.example.orderservice.order.domain.port.outgoing.OrderRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetCustomersService(
	private val orderRepository: OrderRepositoryPort,
) : GetCustomersUseCase {
	@Transactional(readOnly = true)
	override fun invoke() = orderRepository.findDistinctCustomerIds()
}

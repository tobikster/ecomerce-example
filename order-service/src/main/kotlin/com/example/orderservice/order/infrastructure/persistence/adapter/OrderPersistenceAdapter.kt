package com.example.orderservice.order.infrastructure.persistence.adapter

import com.example.orderservice.order.domain.model.CustomerId
import com.example.orderservice.order.domain.model.Order
import com.example.orderservice.order.domain.model.OrderId
import com.example.orderservice.order.domain.port.outgoing.OrderRepositoryPort
import com.example.orderservice.order.infrastructure.persistence.entity.OrderEntity
import com.example.orderservice.order.infrastructure.persistence.mapper.toDomain
import com.example.orderservice.order.infrastructure.persistence.mapper.toEntity
import com.example.orderservice.order.infrastructure.persistence.repository.OrderJpaRepository
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class OrderPersistenceAdapter(
	private val repository: OrderJpaRepository,
) : OrderRepositoryPort {
	override fun save(order: Order) = repository.save(order.toEntity()).toDomain()

	override fun findById(id: OrderId) =
		repository
			.findById(id.value)
			.map(OrderEntity::toDomain)
			.getOrNull()

	override fun findByCustomerId(customerId: CustomerId) =
		repository
			.findAllByCustomerId(customerId.value)
			.map(OrderEntity::toDomain)

	override fun findDistinctCustomerIds() =
		repository
			.findDistinctCustomerIds()
			.map(::CustomerId)
}

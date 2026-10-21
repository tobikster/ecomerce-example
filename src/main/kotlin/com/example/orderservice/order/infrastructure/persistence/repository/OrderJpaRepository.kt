package com.example.orderservice.order.infrastructure.persistence.repository

import com.example.orderservice.order.infrastructure.persistence.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface OrderJpaRepository : JpaRepository<OrderEntity, UUID> {
	fun findAllByCustomerId(customerId: String): List<OrderEntity>

	@Query("SELECT DISTINCT o.customerId FROM OrderEntity o")
	fun findDistinctCustomerIds(): List<String>
}

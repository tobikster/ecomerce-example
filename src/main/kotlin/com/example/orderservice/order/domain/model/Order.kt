package com.example.orderservice.order.domain.model

import java.time.Instant
import java.util.UUID

data class Order private constructor(
	val id: OrderId,
	val customerId: CustomerId,
	val totalAmount: Money,
	val status: OrderStatus,
	val createdAt: Instant,
	val updatedAt: Instant,
) {
	companion object {
		fun create(
			customerId: CustomerId,
			totalAmount: Money,
			now: Instant = Instant.now(),
		) = Order(
			id = OrderId(UUID.randomUUID()),
			customerId = customerId,
			totalAmount = totalAmount,
			status = OrderStatus.CREATED,
			createdAt = now,
			updatedAt = now,
		)

		@Suppress("LongParameterList")
		fun restore(
			id: OrderId,
			customerId: CustomerId,
			totalAmount: Money,
			status: OrderStatus,
			createdAt: Instant,
			updatedAt: Instant,
		) = Order(
			id = id,
			customerId = customerId,
			totalAmount = totalAmount,
			status = status,
			createdAt = createdAt,
			updatedAt = updatedAt,
		)
	}
}

@JvmInline
value class OrderId(
	val value: UUID,
) {
	override fun toString(): String = value.toString().lowercase()
}

@JvmInline
value class CustomerId(
	val value: String,
) {
	init {
		require(value.isNotBlank()) { "Customer ID cannot be blank." }
	}

	override fun toString(): String = value
}

enum class OrderStatus {
	CREATED,
	PAID,
	PROCESSING,
	COMPLETED,
	CANCELLED,
}

package com.example.orderservice.order.infrastructure.persistence.entity

import com.example.orderservice.order.domain.model.OrderStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.Currency
import java.util.UUID

@Suppress("LongParameterList")
@Entity
@Table(name = "orders")
class OrderEntity(
	@Id
	@Column(name = "id", nullable = false, updatable = false)
	val id: UUID,
	@Column(name = "customer_id", nullable = false)
	var customerId: String,
	@Column(name = "total_amount", nullable = false)
	var totalAmount: BigDecimal,
	@Column(name = "currency", nullable = false)
	var currency: Currency,
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	var status: OrderStatus,
	@Column(name = "created_at", nullable = false, updatable = false)
	val createdAt: Instant,
	@Column(name = "updated_at", nullable = false)
	var updatedAt: Instant,
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is OrderEntity) return false
		return id == other.id
	}

	override fun hashCode(): Int = id.hashCode()

	override fun toString() =
		"OrderEntity(" +
			"id=$id, " +
			"customerId=$customerId, " +
			"totalAmount=$totalAmount, " +
			"currency=$currency, " +
			"status=$status, " +
			"createdAt=$createdAt, " +
			"updatedAt=$updatedAt" +
			")"
}

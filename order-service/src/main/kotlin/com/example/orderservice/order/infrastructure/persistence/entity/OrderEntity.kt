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

@Entity
@Table(name = "orders")
@Suppress("LongParameterList")
class OrderEntity(
	@Id
	@Column(name = "id", nullable = false)
	val id: UUID,
	@Column(name = "customer_id", nullable = false)
	val customerId: String,
	@Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
	val totalAmount: BigDecimal,
	@Column(name = "currency", nullable = false, length = 3)
	val currency: Currency,
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	var status: OrderStatus,
	@Column(name = "payment_id")
	var paymentId: UUID?,
	@Column(name = "created_at", nullable = false, updatable = false)
	val createdAt: Instant,
	@Column(name = "updated_at", nullable = false)
	var updatedAt: Instant,
) {
	override fun toString(): String =
		"OrderEntity(" +
			"id=$id, " +
			"customerId=$customerId, " +
			"totalAmount=$totalAmount, " +
			"currency=$currency, " +
			"status=$status, " +
			"paymentId=$paymentId, " +
			"createdAt=$createdAt, " +
			"updatedAt=$updatedAt" +
			")"
}

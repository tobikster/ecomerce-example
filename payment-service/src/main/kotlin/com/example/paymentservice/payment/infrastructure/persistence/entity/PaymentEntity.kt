package com.example.paymentservice.payment.infrastructure.persistence.entity

import com.example.paymentservice.payment.domain.model.PaymentStatus
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
@Table(name = "payments")
@Suppress("LongParameterList")
class PaymentEntity(
	@Id
	@Column(name = "id", nullable = false, updatable = false)
	val id: UUID,
	@Column(name = "order_id", nullable = false)
	var orderId: UUID,
	@Column(name = "customer_id", nullable = false)
	var customerId: String,
	@Column(name = "amount", nullable = false)
	var amount: BigDecimal,
	@Column(name = "currency", nullable = false)
	var currency: Currency,
	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	var status: PaymentStatus,
	@Column(name = "failure_reason")
	var failureReason: String?,
	@Column(name = "created_at", nullable = false, updatable = false)
	val createdAt: Instant,
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is PaymentEntity) return false
		return id == other.id
	}

	override fun hashCode(): Int = id.hashCode()

	override fun toString() =
		"""
		PaymentEntity(
			id=$id,
			orderId=$orderId,
			customerId=$customerId,
			amount=$amount,
			currency=$currency,
			status=$status,
			failureReason=$failureReason,
			createdAt=$createdAt
		)
		""".trimIndent()
}

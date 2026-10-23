package com.example.paymentservice.payment.domain.model

import com.example.paymentservice.common.domain.model.CustomerId
import com.example.paymentservice.common.domain.model.Money
import com.example.paymentservice.common.domain.model.OrderId
import java.time.Instant
import java.util.UUID

data class Payment private constructor(
	val id: PaymentId,
	val orderId: OrderId,
	val customerId: CustomerId,
	val amount: Money,
	val status: PaymentStatus,
	val failureReason: String?,
	val createdAt: Instant,
) {
	companion object {
		fun completed(
			orderId: OrderId,
			customerId: CustomerId,
			amount: Money,
			now: Instant = Instant.now(),
		) = Payment(
			id = PaymentId(UUID.randomUUID()),
			orderId = orderId,
			customerId = customerId,
			amount = amount,
			status = PaymentStatus.COMPLETED,
			failureReason = null,
			createdAt = now,
		)

		fun failed(
			orderId: OrderId,
			customerId: CustomerId,
			amount: Money,
			failureReason: String?,
			now: Instant = Instant.now(),
		) = Payment(
			id = PaymentId(UUID.randomUUID()),
			orderId = orderId,
			customerId = customerId,
			amount = amount,
			status = PaymentStatus.FAILED,
			failureReason = failureReason,
			createdAt = now,
		)

		@Suppress("LongParameterList")
		fun restore(
			id: PaymentId,
			orderId: OrderId,
			customerId: CustomerId,
			amount: Money,
			status: PaymentStatus,
			failureReason: String?,
			createdAt: Instant,
		) = Payment(
			id = id,
			orderId = orderId,
			customerId = customerId,
			amount = amount,
			status = status,
			failureReason = failureReason,
			createdAt = createdAt,
		)
	}
}

@JvmInline
value class PaymentId(
	val value: UUID = UUID.randomUUID(),
)

enum class PaymentStatus {
	COMPLETED,
	FAILED,
}

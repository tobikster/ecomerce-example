package com.example.orderservice.order.domain.model

import arrow.core.Either
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Order private constructor(
	val id: OrderId,
	val customerId: CustomerId,
	val totalAmount: Money,
	val status: OrderStatus,
	val paymentId: PaymentId?,
	val createdAt: Instant,
	val updatedAt: Instant,
) {
	fun withConfirmedPayment(paymentId: PaymentId): Either<OrderFailure, Order> =
		either {
			if (status == OrderStatus.PAID && this.paymentId == paymentId) {
				return@either this
			}

			if (status != OrderStatus.CREATED) {
				raise(OrderFailure.InvalidStatus(status))
			}

			copy(
				status = OrderStatus.PAID,
				paymentId = paymentId,
				updatedAt = Instant.now(),
			)
		}

	fun withFailedPayment(paymentId: PaymentId): Either<OrderFailure, Order> =
		either {
			if (status == OrderStatus.CANCELLED && this.paymentId == paymentId) {
				return@either this
			}

			if (status != OrderStatus.CREATED) {
				raise(OrderFailure.InvalidStatus(status))
			}

			copy(
				status = OrderStatus.CANCELLED,
				paymentId = paymentId,
				updatedAt = Instant.now(),
			)
		}

	companion object {
		fun create(
			customerId: CustomerId,
			totalAmount: Money,
			now: Instant = Instant.now(),
		): Order {
			if (totalAmount.amount <= BigDecimal.ZERO) throw OrderException.InvalidAmount(totalAmount.amount)

			return Order(
				id = OrderId(UUID.randomUUID()),
				customerId = customerId,
				totalAmount = totalAmount,
				status = OrderStatus.CREATED,
				paymentId = null,
				createdAt = now,
				updatedAt = now,
			)
		}

		@Suppress("LongParameterList")
		fun restore(
			id: OrderId,
			customerId: CustomerId,
			totalAmount: Money,
			status: OrderStatus,
			paymentId: PaymentId?,
			createdAt: Instant,
			updatedAt: Instant,
		) = Order(
			id = id,
			customerId = customerId,
			totalAmount = totalAmount,
			status = status,
			paymentId = paymentId,
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

sealed interface OrderFailure {
	data class InvalidStatus(
		val status: OrderStatus,
	) : OrderFailure
}

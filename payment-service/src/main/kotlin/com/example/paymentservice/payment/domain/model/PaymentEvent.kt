package com.example.paymentservice.payment.domain.model

import com.example.paymentservice.common.domain.model.Money
import com.example.paymentservice.common.domain.model.OrderId
import java.time.Instant

sealed interface PaymentEvent {
	val paymentId: PaymentId
	val orderId: OrderId
	val createdAt: Instant

	data class Completed(
		override val paymentId: PaymentId,
		override val orderId: OrderId,
		val amount: Money,
		override val createdAt: Instant = Instant.now(),
	) : PaymentEvent

	data class Failed(
		override val paymentId: PaymentId,
		override val orderId: OrderId,
		val amount: Money,
		val failureReason: String,
		override val createdAt: Instant = Instant.now(),
	) : PaymentEvent
}

fun Payment.toEvent() =
	when (status) {
		PaymentStatus.COMPLETED -> {
			PaymentEvent.Completed(
				paymentId = id,
				orderId = orderId,
				amount = amount,
			)
		}

		PaymentStatus.FAILED -> {
			PaymentEvent.Failed(
				paymentId = id,
				orderId = orderId,
				amount = amount,
				failureReason = failureReason.orEmpty(),
			)
		}
	}

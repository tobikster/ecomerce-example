package com.example.paymentservice.payment.infrastructure.outbox.dto

import com.example.paymentservice.payment.domain.model.PaymentEvent
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PaymentCompletedEventDto(
	val paymentId: UUID,
	val orderId: UUID,
	val amount: BigDecimal,
	val currency: String,
	val createdAt: Instant,
)

fun PaymentEvent.Completed.toDto() =
	PaymentCompletedEventDto(
		paymentId = paymentId.value,
		orderId = orderId.value,
		amount = amount.amount,
		currency = amount.currency.currencyCode,
		createdAt = createdAt,
	)

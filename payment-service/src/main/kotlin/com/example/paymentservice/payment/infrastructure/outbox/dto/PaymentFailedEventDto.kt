package com.example.paymentservice.payment.infrastructure.outbox.dto

import com.example.paymentservice.payment.domain.model.PaymentEvent
import java.math.BigDecimal
import java.util.UUID

data class PaymentFailedEventDto(
	val paymentId: UUID,
	val orderId: UUID,
	val amount: BigDecimal,
	val currency: String,
	val failureReason: String,
)

fun PaymentEvent.Failed.toDto() =
	PaymentFailedEventDto(
		paymentId = paymentId.value,
		orderId = orderId.value,
		amount = amount.amount,
		currency = amount.currency.currencyCode,
		failureReason = failureReason,
	)

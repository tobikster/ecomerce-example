package com.example.orderservice.order.infrastructure.kafka.dto

import java.math.BigDecimal
import java.util.UUID

data class PaymentCompletedEventDto(
	val paymentId: UUID,
	val orderId: UUID,
	val amount: BigDecimal,
	val currency: String,
)

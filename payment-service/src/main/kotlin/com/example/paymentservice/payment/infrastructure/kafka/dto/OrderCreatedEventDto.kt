package com.example.paymentservice.payment.infrastructure.kafka.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class OrderCreatedEventDto(
	val orderId: UUID,
	val customerId: String,
	val amount: BigDecimal,
	val currency: String,
	val createdAt: Instant,
)

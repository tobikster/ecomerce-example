package com.example.orderservice.order.infrastructure.kafka.dto

import com.example.orderservice.order.domain.model.OrderCreatedEvent
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

fun OrderCreatedEvent.toDto() =
	OrderCreatedEventDto(
		orderId = orderId.value,
		customerId = customerId.value,
		amount = amount,
		currency = currency.currencyCode,
		createdAt = createdAt,
	)

package com.example.orderservice.order.infrastructure.rest.dto

import com.example.orderservice.order.domain.model.Order
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class OrderResponse(
	val id: UUID,
	val customerId: String,
	val amount: BigDecimal,
	val currencyCode: String,
	val status: String,
	val createdAt: Instant,
	val updatedAt: Instant,
)

fun Order.toResponse() =
	OrderResponse(
		id = id.value,
		customerId = customerId.value,
		amount = totalAmount.amount,
		currencyCode = totalAmount.currency.currencyCode,
		status = status.name.lowercase(),
		createdAt = createdAt,
		updatedAt = updatedAt,
	)

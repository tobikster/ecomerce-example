package com.example.orderservice.order.infrastructure.rest.dto

import com.example.orderservice.order.domain.model.Order
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Schema(description = "Order details response")
data class OrderResponse(
	@field:Schema(description = "Unique order identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	val id: UUID,
	@field:Schema(description = "Unique customer identifier", example = "customer_123")
	val customerId: String,
	@field:Schema(description = "Total order amount", example = "149.99")
	val amount: BigDecimal,
	@field:Schema(description = "3-letter ISO 4217 currency code", example = "EUR")
	val currencyCode: String,
	@field:Schema(
		description = "Order status",
		example = "created",
		allowableValues = ["created", "paid", "processing", "completed", "cancelled"],
	)
	val status: String,
	@field:Schema(description = "Order creation timestamp", example = "2026-10-20T12:00:00Z")
	val createdAt: Instant,
	@field:Schema(description = "Order last update timestamp", example = "2026-10-20T12:00:00Z")
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

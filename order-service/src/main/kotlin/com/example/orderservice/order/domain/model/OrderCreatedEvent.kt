package com.example.orderservice.order.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.util.Currency

data class OrderCreatedEvent(
	val orderId: OrderId,
	val customerId: CustomerId,
	val amount: BigDecimal,
	val currency: Currency,
	val createdAt: Instant,
)

fun Order.toCreatedEvent() =
	OrderCreatedEvent(
		orderId = id,
		customerId = customerId,
		amount = totalAmount.amount,
		currency = totalAmount.currency,
		createdAt = createdAt,
	)

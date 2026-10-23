package com.example.orderservice.order.domain.model

import java.math.BigDecimal

sealed class OrderException(
	message: String,
) : RuntimeException(message) {
	class InvalidAmount(
		amount: BigDecimal,
	) : OrderException("Order amount must be positive, got $amount")
}

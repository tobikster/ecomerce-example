package com.example.orderservice.order.domain.port.incoming

import com.example.orderservice.order.domain.model.Order
import java.math.BigDecimal
import java.util.Currency

interface CreateOrderUseCase {
	operator fun invoke(command: CreateOrderCommand): Order
}

data class CreateOrderCommand(
	val customerId: String,
	val amount: BigDecimal,
	val currency: Currency,
)

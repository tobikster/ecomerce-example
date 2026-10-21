package com.example.orderservice.order.infrastructure.rest.dto

import com.example.orderservice.order.domain.port.incoming.CreateOrderCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.Currency

data class CreateOrderRequest(
	@field:NotBlank
	@field:Size(max = 64)
	val customerId: String,
	val amount: BigDecimal,
	@field:Size(min = 3, max = 3)
	val currencyCode: String,
)

fun CreateOrderRequest.toCommand() =
	CreateOrderCommand(
		customerId = customerId,
		amount = amount,
		currency = Currency.getInstance(currencyCode),
	)

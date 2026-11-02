package com.example.orderservice.order.infrastructure.rest.dto

import com.example.orderservice.order.domain.port.incoming.CreateOrderCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.Currency

@Schema(description = "Request body for creating a new order")
data class CreateOrderRequest(
	@field:NotBlank
	@field:Size(max = 64)
	@field:Schema(
		description = "Unique customer identifier",
		example = "customer_123",
		requiredMode = Schema.RequiredMode.REQUIRED,
	)
	val customerId: String,
	@field:Schema(
		description = "Total order amount",
		example = "149.99",
		requiredMode = Schema.RequiredMode.REQUIRED,
	)
	val amount: BigDecimal,
	@field:Size(min = 3, max = 3)
	@field:Schema(
		description = "3-letter ISO 4217 currency code",
		example = "EUR",
		requiredMode = Schema.RequiredMode.REQUIRED,
	)
	val currencyCode: String,
)

fun CreateOrderRequest.toCommand() =
	CreateOrderCommand(
		customerId = customerId,
		amount = amount,
		currency = Currency.getInstance(currencyCode),
	)

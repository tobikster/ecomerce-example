package com.example.paymentservice.account.infrastructure.rest.dto

import com.example.paymentservice.account.domain.model.CustomerId
import com.example.paymentservice.account.domain.model.of
import com.example.paymentservice.account.domain.port.incoming.WithdrawCommand
import com.example.paymentservice.infrastructure.rest.validation.ValidCurrencyCode
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class WithdrawRequest(
	@field:Positive
	@field:Schema(
		description = "Deposit amount",
		example = "100.00",
		requiredMode = Schema.RequiredMode.REQUIRED,
	)
	val amount: BigDecimal,
	@field:Size(min = 3, max = 3)
	@field:Schema(
		description = "3-letter ISO4217 currency code",
		example = "PLN",
		requiredMode = Schema.RequiredMode.REQUIRED,
	)
	@field:ValidCurrencyCode
	val currencyCode: String,
)

fun WithdrawRequest.toCommand(customerId: String) =
	WithdrawCommand(
		customerId = CustomerId(customerId),
		funds = amount of currencyCode,
	)

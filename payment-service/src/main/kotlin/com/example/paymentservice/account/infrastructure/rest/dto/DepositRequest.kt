package com.example.paymentservice.account.infrastructure.rest.dto

import com.example.paymentservice.account.domain.port.incoming.DepositCommand
import com.example.paymentservice.common.domain.model.CustomerId
import com.example.paymentservice.common.domain.model.of
import com.example.paymentservice.infrastructure.rest.validation.ValidCurrencyCode
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class DepositRequest(
	@field:Positive
	@field:Schema(
		description = "Deposit amount",
		example = "100.00",
		requiredMode = Schema.RequiredMode.REQUIRED,
	)
	val funds: BigDecimal,
	@field:Size(min = 3, max = 3)
	@field:Schema(
		description = "3-letter ISO4217 currency code",
		example = "PLN",
		requiredMode = Schema.RequiredMode.REQUIRED,
	)
	@field:ValidCurrencyCode
	val currencyCode: String,
)

fun DepositRequest.toCommand(customerId: String) =
	DepositCommand(
		customerId = CustomerId(customerId),
		funds = funds of currencyCode,
	)

package com.example.paymentservice.account.infrastructure.rest.dto

import com.example.paymentservice.account.domain.model.Account
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class AccountResponse(
	val id: UUID,
	val customerId: String,
	val balance: BigDecimal,
	val currencyCode: String,
	val createdAt: Instant,
	val updatedAt: Instant,
)

fun Account.toResponse() =
	AccountResponse(
		id = id.value,
		customerId = customerId.value,
		balance = balance.amount,
		currencyCode = balance.currency.currencyCode,
		createdAt = createdAt,
		updatedAt = updatedAt,
	)

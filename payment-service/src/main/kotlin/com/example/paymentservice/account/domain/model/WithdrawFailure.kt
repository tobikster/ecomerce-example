package com.example.paymentservice.account.domain.model

import com.example.paymentservice.common.domain.model.Money
import java.util.Currency

sealed interface WithdrawFailure {
	data class CurrencyMismatch(
		val accountCurrency: Currency,
		val requestedCurrency: Currency,
	) : WithdrawFailure

	data class InsufficientFunds(
		val requested: Money,
		val available: Money,
	) : WithdrawFailure
}

package com.example.paymentservice.account.domain.model

import com.example.paymentservice.common.domain.model.Money
import java.util.Currency

sealed interface WithdrawResult {
	data class Success(
		val account: Account,
	) : WithdrawResult

	sealed interface Failure : WithdrawResult {
		data class CurrencyMismatch(
			val accountCurrency: Currency,
			val requestedCurrency: Currency,
		) : Failure

		data class InsufficientFunds(
			val requested: Money,
			val available: Money,
		) : Failure
	}
}

inline fun WithdrawResult.onSuccess(action: (WithdrawResult.Success) -> Unit) =
	also {
		if (this is WithdrawResult.Success) action(this)
	}

inline fun <R> WithdrawResult.fold(
	onSuccess: (WithdrawResult.Success) -> R,
	onFailure: (WithdrawResult.Failure) -> R,
): R =
	when (this) {
		is WithdrawResult.Success -> onSuccess(this)
		is WithdrawResult.Failure -> onFailure(this)
	}

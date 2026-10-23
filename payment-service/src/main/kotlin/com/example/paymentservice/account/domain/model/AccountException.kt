package com.example.paymentservice.account.domain.model

import com.example.paymentservice.common.domain.model.Money

sealed class AccountException(
	message: String,
) : RuntimeException(message) {
	class InsufficientFunds(
		requested: Money,
		available: Money,
	) : AccountException("Insufficient funds: $requested requested, but $available is available".format())
}

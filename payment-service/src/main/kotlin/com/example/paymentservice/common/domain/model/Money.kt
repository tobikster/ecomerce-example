package com.example.paymentservice.common.domain.model

import java.math.BigDecimal
import java.util.Currency

data class Money(
	val amount: BigDecimal,
	val currency: Currency,
) {
	init {
		require(amount >= BigDecimal.ZERO) { "Amount cannot be negative!" }
	}

	operator fun plus(other: Money): Money {
		require(currency == other.currency) {
			"Currency mismatch: cannot add ${other.currency} to $currency!"
		}

		return (amount + other.amount) of currency
	}

	operator fun minus(other: Money): Money {
		require(currency == other.currency) {
			"Currency mismatch: cannot subtract ${other.currency} from $currency!"
		}

		return (amount - other.amount) of currency
	}

	override fun toString() = "$amount $currency"

	companion object {
		fun zero(currency: Currency) = BigDecimal.ZERO of currency
	}
}

infix fun BigDecimal.of(currency: Currency) = Money(this, currency)

infix fun BigDecimal.of(currencyCode: String) = this of Currency.getInstance(currencyCode)

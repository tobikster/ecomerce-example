package com.example.orderservice.order.domain.model

import java.math.BigDecimal
import java.util.Currency

data class Money(
	val amount: BigDecimal,
	val currency: Currency,
) {
	operator fun plus(other: Money): Money {
		require(currency == other.currency) {
			"Cannot add money with different currencies: $currency and ${other.currency}"
		}

		return Money(amount + other.amount, currency)
	}

	operator fun minus(other: Money): Money {
		require(currency == other.currency) {
			"Cannot subtract money with different currencies: $currency and ${other.currency}"
		}

		return Money(amount - other.amount, currency)
	}
}

infix fun BigDecimal.of(currency: Currency) = Money(this, currency)

infix fun BigDecimal.of(currencyCode: String) = this of Currency.getInstance(currencyCode)
